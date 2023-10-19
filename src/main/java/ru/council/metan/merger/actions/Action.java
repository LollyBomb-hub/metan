package ru.council.metan.merger.actions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import ru.council.metan.enums.JsonElementType;
import ru.council.metan.exceptions.ProcessingException;
import ru.council.metan.jsonpath.JsonPath;
import ru.council.metan.models.MetaJson;

@AllArgsConstructor
@Slf4j
@Getter
@ToString
public class Action {

    private ActionType type;
    private JsonPath targetJsonPath;
    private JsonPath sourceJsonPath;

    public void apply(MetaJson target, MetaJson source) {
        log.debug("Target: {}", target);
        log.debug("Source: {}", source);
        target = target.resolve(targetJsonPath.exceptLeaf());
        source = source.resolve(sourceJsonPath);
        switch (type) {
            case PUT:
                if (target.isNode()) {
                    target.put(targetJsonPath.leaf().getPath(), source);
                } else {
                    throw new IllegalStateException("No way to put by key in non-node element!");
                }
                break;
            case APPEND:
                MetaJson resolved = target.resolve(targetJsonPath.leaf());
                if (resolved == null) {
                    target.put(targetJsonPath.leaf().getPath(), source);
                } else if (resolved.isArray()) {
                    if (source.isArray()) {
                        for (MetaJson sourceElement: source.getArray()) {
                            resolved.add(sourceElement);
                        }
                    } else {
                        resolved.add(source);
                    }
                } else if (resolved.isNode()) {
                    MetaJson array = new MetaJson(JsonElementType.Array);
                    array.add(resolved);
                    if (source.isArray()) {
                        for (MetaJson sourceElement: source.getArray()) {
                            array.add(sourceElement);
                        }
                    } else {
                        array.add(source);
                    }
                    target.put(targetJsonPath.leaf().getPath(), array);
                } else {
                    throw new ProcessingException("Not available yet!");
                }
                break;
            default:
                throw new IllegalStateException("Could not resolve action type");
        }
    }

}
