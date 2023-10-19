package ru.council.metan.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.RuntimeJsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import ru.council.metan.enums.JsonElementType;
import ru.council.metan.models.MetaJson;
import ru.council.metan.models.Scalar;

import java.io.IOException;
import java.util.Iterator;

/**
 * Object mapper deserializer<br/>
 * Has static method registerToObjectMapperInstance for registering self in given om instance<br/>
 */
public class MetaJsonStdDeserializer extends StdDeserializer<MetaJson> {

    public MetaJsonStdDeserializer() {
        this(MetaJson.class);
    }

    protected MetaJsonStdDeserializer(Class<?> vc) {
        super(vc);
    }

    public static void registerToObjectMapperInstance(ObjectMapper objectMapper) {
        SimpleModule sm = new SimpleModule();
        sm.addDeserializer(MetaJson.class, new MetaJsonStdDeserializer());
        objectMapper.registerModule(sm);
    }

    @Override
    public MetaJson deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        return getMetaJsonFromJsonNode(node);
    }

    public MetaJson getMetaJsonFromJsonNode(JsonNode node) {
        if (node.isArray()) {
            MetaJson result = new MetaJson(JsonElementType.Array);
            for (JsonNode arrayElement : node) {
                result.add(getMetaJsonFromJsonNode(arrayElement));
            }
            return result;
        } else if (node.isContainerNode()) {
            MetaJson result = new MetaJson(JsonElementType.Node);
            for (Iterator<String> it = node.fieldNames(); it.hasNext(); ) {
                String innerNode = it.next();
                JsonNode innerJsonNode = node.get(innerNode);
                result.put(innerNode, getMetaJsonFromJsonNode(innerJsonNode));
            }
            return result;
        } else if (node.isValueNode()) {
            MetaJson result = new MetaJson(JsonElementType.Scalar);
            switch (node.getNodeType()) {
                case STRING:
                    result.setJson(new Scalar<>(String.class, node.asText()));
                    break;
                case BOOLEAN:
                    result.setJson(new Scalar<>(Boolean.class, node.asBoolean()));
                    break;
                case NUMBER:
                    if (node.isInt()) {
                        result.setJson(new Scalar<>(Integer.class, node.asInt()));
                    } else if (node.isLong()) {
                        result.setJson(new Scalar<>(Long.class, node.asLong()));
                    } else if (node.isFloat() || node.isDouble()) {
                        result.setJson(new Scalar<>(Double.class, node.asDouble()));
                    }
                    break;
                case NULL:
                    result.setJson(null);
                    break;
                default:
                    throw new RuntimeJsonMappingException("Non scalar \"scalar\": " + node);
            }
            return result;
        }
        return null;
    }
}
