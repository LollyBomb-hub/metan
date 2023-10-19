package ru.council.metan.jsonpath;

import java.util.ArrayList;
import java.util.List;

public class JsonPath {

    private final List<JsonPathElement> pathElements = new ArrayList<>();

    public JsonPath() {
        construct("");
    }

    public JsonPath(String path) {
        if (path == null) {
            construct("");
        } else if (!path.equals("")) {
            construct(path);
        } else {
            construct("");
        }
    }

    public JsonPath(List<JsonPathElement> pathElements) {
        this.pathElements.addAll(pathElements);
    }

    private void construct(String path) {
        int current = path.indexOf(".");
        while (current != -1) {
            pathElements.add(new JsonPathElement(path.substring(0, current)));
            path = path.substring(current + 1);
            current = path.indexOf(".");
        }

        if (!path.isBlank()) {
            pathElements.add(new JsonPathElement(path, true));
        }
    }

    public int size() {
        return pathElements.size();
    }

    public JsonPathElement get(int index) {
        if (index >= size()) return null;
        return pathElements.get(index);
    }

    public void add(JsonPathElement jpe) {
        pathElements.forEach(el -> el.setLeaf(false));
        jpe.setLeaf(true);
        pathElements.add(jpe);
    }

    public JsonPath exceptLeaf() {
        if (pathElements.size() == 0) {
            return new JsonPath();
        }
        return new JsonPath(pathElements.subList(0, pathElements.size() - 1));
    }

    public JsonPath leaf() {
        if (pathElements.size() == 0) {
            return new JsonPath();
        }
        return new JsonPath(pathElements.get(pathElements.size() - 1).getSource());
    }

    public JsonPath copyWithElement(JsonPathElement jpe) {
        String path = getPath();
        JsonPath copied = new JsonPath(path);
        copied.add(jpe);
        return copied;
    }

    public JsonPath copyWithElement(String identifier) {
        return copyWithElement(new JsonPathElement(identifier));
    }

    public JsonPath copyWithPath(String path) {
        if (path == null) {
            return new JsonPath(getPath());
        } else if (path.isBlank()) {
            return new JsonPath(getPath());
        } else {
            if (getPath().isBlank()) {
                return new JsonPath(path);
            }
            return new JsonPath(getPath() + "." + path);
        }
    }

    public String getPath() {
        StringBuilder path = new StringBuilder();

        for (JsonPathElement jpe : pathElements) {
            path.append(jpe.getSource());
            if (pathElements.indexOf(jpe) + 1 < size()) path.append(".");
        }

        return path.toString();
    }

    public String getPath(int fromElement) {
        StringBuilder path = new StringBuilder();

        for (int index = fromElement; index < size(); index++) {
            path.append(pathElements.get(index).getSource());
            if (index + 1 < size()) path.append(".");
        }

        return path.toString();
    }

    @Override
    public String toString() {
        return getPath();
    }
}
