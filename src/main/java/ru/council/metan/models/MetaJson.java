package ru.council.metan.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ru.council.metan.enums.JsonElementType;
import ru.council.metan.jsonpath.JsonPath;
import ru.council.metan.jsonpath.JsonPathElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Slf4j
@SuppressWarnings("unused")
public class MetaJson {

    @Setter
    @Getter
    @JsonIgnore
    private MetaJson parent;
    @Getter
    @JsonIgnore
    private JsonElementType type;
    @Setter
    @Getter
    @JsonValue
    private Object json;

    @JsonIgnore
    public MetaJson(JsonElementType type, Object json) {
        this.type = type;
        this.json = json;
    }

    @JsonIgnore
    public MetaJson(JsonElementType type) {
        this.type = type;
        switch (type) {
            case Node:
                json = new HashMap<String, MetaJson>();
                break;
            case Array:
                json = new ArrayList<MetaJson>();
                break;
            case Scalar:
                json = null;
                break;
            default:
                throw new IllegalStateException("Cannot create MetaJson of type " + type);
        }
    }

    @JsonIgnore
    public boolean isEmpty() {
        if (json == null) {
            throw new IllegalStateException("Cannot access null value");
        } else if (isArray()) {
            return getArray().isEmpty();
        } else if (isNode()) {
            return getNode().isEmpty();
        } else {
            throw new IllegalStateException("Cannot check for emptiness in scalar");
        }
    }

    @JsonIgnore
    public boolean isArray() {
        return type.equals(JsonElementType.Array);
    }

    @JsonIgnore
    public boolean isNode() {
        return type.equals(JsonElementType.Node);
    }

    @JsonIgnore
    public boolean isScalar() {
        return type.equals(JsonElementType.Scalar);
    }


    @SuppressWarnings("unchecked")
    @JsonIgnore
    public List<MetaJson> getArray() {
        if (isArray()) {
            return (List<MetaJson>) json;
        }
        throw new IllegalCallerException("Cannot return array from non array type json");
    }

    @SuppressWarnings("unchecked")
    @JsonIgnore
    public Map<String, MetaJson> getNode() {
        if (isNode()) {
            return (Map<String, MetaJson>) json;
        }
        throw new IllegalCallerException("Cannot return node from non-node type json");
    }

    @JsonIgnore
    public Scalar<?> getScalar() {
        if (isScalar()) {
            return (Scalar<?>) json;
        }
        throw new IllegalCallerException("Cannot return scalar from non-scalar type json");
    }

    @JsonIgnore
    public <T> MetaJson put(String key, T value) {
        if (isNode()) {
            Map<String, MetaJson> node = getNode();
            if (value instanceof MetaJson) {
                ((MetaJson) value).setParent(this);
                return node.put(key, (MetaJson) value);
            } else if (value instanceof Scalar) {
                return node.put(key, new MetaJson(this, JsonElementType.Scalar, value));
            } else {
                throw new UnsupportedOperationException("Unable to set undetermined type!");
            }
        }
        throw new IllegalStateException("Cannot put not to node");
    }

    @JsonIgnore
    public MetaJson putScalar(String key, Object value) {
        if (isNode()) {
            Map<String, MetaJson> node = getNode();
            if (value instanceof Scalar) {
                return node.put(key, new MetaJson(this, JsonElementType.Scalar, value));
            } else if (value instanceof MetaJson) {
                ((MetaJson) value).setParent(this);
                return node.put(key, (MetaJson) value);
            } else {
                return node.put(key, new MetaJson(this, JsonElementType.Scalar, new Scalar<>(value)));
            }
        }
        throw new IllegalStateException("Cannot put not to node");
    }

    @JsonIgnore
    public void add(MetaJson value) {
        if (isArray()) {
            List<MetaJson> array = getArray();
            value.setParent(this);
            array.add(value);
            return;
        }
        throw new IllegalStateException("Cannot add not to array");
    }

    @JsonIgnore
    public void remove(String key) {
        if (isNode()) {
            this.getNode().remove(key);
            return;
        }
        throw new UnsupportedOperationException("Cannot remove not from node");
    }

    @JsonIgnore
    public MetaJson resolve(String path) {
        return resolve(new JsonPath(path));
    }

    @JsonIgnore
    public MetaJson resolveWithCreation(String path) {
        return resolveWithCreation(new JsonPath(path));
    }

    @JsonIgnore
    public MetaJson resolveWithCreation(JsonPath path) {
        if (path.size() > 0) {
            JsonPathElement next = path.get(0);

            log.debug("Json path element: {}", next.getSource());

            switch (next.getType()) {
                case Identifier: {
                    if (next.isParentReference()) {
                        if (next.isLeaf()) {
                            return getParent();
                        } else {
                            return getParent().resolveWithCreation(path.getPath(1));
                        }
                    }
                    if (isNode()) {
                        String identifier = next.getIdentifier();
                        Map<String, MetaJson> node = getNode();
                        if (node.containsKey(identifier)) {
                            MetaJson resolved = node.get(identifier);
                            if (next.isLeaf()) {
                                return resolved;
                            }
                            if (resolved == null) return null;
                            return resolved.resolveWithCreation(path.getPath(1));
                        } else {
                            if (next.isLeaf()) {
                                MetaJson created = new MetaJson(JsonElementType.Scalar, new Scalar<>(null, null));
                                this.put(identifier, created);
                                return created;
                            }
                            MetaJson created = new MetaJson(JsonElementType.Node);
                            this.put(identifier, created);
                            return created.resolveWithCreation(path.getPath(1));
                        }
                    } else {
                        throw new UnsupportedOperationException("Unable to get value by string key from non-node element");
                    }
                }
                case ArraySubscriptor: {
                    switch (next.getArraySubscriptionType()) {
                        case None:
                            throw new IllegalStateException("'None' subscription type for array subscriptor!");
                        case All:
                            if (isArray()) {
                                List<MetaJson> resolved = new ArrayList<>();

                                for (MetaJson selfElement : getArray()) {
                                    MetaJson resolvedElement = selfElement.resolveWithCreation(path.getPath(1));
                                    resolved.add(resolvedElement);
                                }

                                return new MetaJson(JsonElementType.Array, resolved);
                            } else {
                                throw new IllegalStateException("Invalid path " + path.getPath(1));
                            }
                        case IndexedByStringKey:
                            if (isNode()) {
                                Map<String, MetaJson> node = getNode();

                                if (node.containsKey(next.getIndexKey())) {
                                    MetaJson resolved = node.get(next.getIndexKey());
                                    if (next.isLeaf()) {
                                        return resolved;
                                    } else {
                                        if (resolved != null)
                                            return resolved.resolve(path.getPath(1));
                                        return null;
                                    }
                                } else {
                                    MetaJson created = new MetaJson(JsonElementType.Node);
                                    this.put(next.getIndexKey(), created);
                                    if (next.isLeaf()) {
                                        return created;
                                    } else {
                                        return created.resolveWithCreation(path.getPath(1));
                                    }
                                }
                            } else {
                                throw new IllegalStateException("String subscription '" + next + "' is only available for nodes");
                            }
                        case Indexed:
                            if (isArray()) {
                                while (getArray().size() < next.getIndex() + 1) {
                                    if (next.isLeaf()) {
                                        getArray().add(new MetaJson(JsonElementType.Scalar));
                                    } else {
                                        getArray().add(new MetaJson(JsonElementType.Node));
                                    }
                                }
                                if (next.isLeaf()) {
                                    return getArray().get(next.getIndex());
                                } else {
                                    return getArray().get(next.getIndex()).resolveWithCreation(path.getPath(1));
                                }
                            } else {
                                throw new IllegalStateException("Invalid path " + path.getPath());
                            }
                    }
                    break;
                }
            }
        }
        return null;
    }

    @JsonIgnore
    public MetaJson resolve(JsonPath path) {
        if (path.size() > 0) {
            JsonPathElement next = path.get(0);

            switch (next.getType()) {
                case Identifier:
                    if (next.isParentReference()) {
                        if (next.isLeaf()) {
                            return getParent();
                        } else {
                            return getParent().resolve(path.getPath(1));
                        }
                    }
                    if (isNode()) {
                        Map<String, MetaJson> node = getNode();
                        MetaJson resolved = node.get(next.getIdentifier());
                        if (next.isLeaf()) {
                            return resolved;
                        }
                        if (resolved == null) return null;
                        return resolved.resolve(path.getPath(1));
                    } else {
                        throw new UnsupportedOperationException("Unable to get value by string key from non-node element");
                    }
                case ArraySubscriptor:
                    switch (next.getArraySubscriptionType()) {
                        case None:
                            throw new IllegalStateException("'None' subscription type for array subscriptor!");
                        case All:
                            if (isArray()) {
                                List<MetaJson> resolved = new ArrayList<>();

                                for (MetaJson selfElement : getArray()) {
                                    MetaJson resolvedElement = selfElement.resolve(path.getPath(1));
                                    resolved.add(resolvedElement);
                                }

                                return new MetaJson(JsonElementType.Array, resolved);
                            } else {
                                throw new IllegalStateException("Invalid path " + path.getPath(1));
                            }
                        case IndexedByStringKey:
                            if (isNode()) {
                                Map<String, MetaJson> node = getNode();

                                MetaJson resolved = node.get(next.getIndexKey());
                                if (next.isLeaf()) {
                                    return resolved;
                                } else {
                                    if (resolved != null)
                                        return resolved.resolve(path.getPath(1));
                                    return null;
                                }
                            } else {
                                throw new IllegalStateException("String subscription '" + next + "' is only available for nodes");
                            }
                        case Indexed:
                            if (isArray()) {
                                if (next.isLeaf()) {
                                    return getArray().get(next.getIndex());
                                } else {
                                    return getArray().get(next.getIndex()).resolve(path.getPath(1));
                                }
                            } else {
                                throw new IllegalStateException("Invalid path " + path.getPath());
                            }
                    }
                    break;
            }
        }
        return this;
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "Error representation!";
        }
    }
}
