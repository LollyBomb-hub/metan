package ru.council.metan.merger;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import ru.council.metan.jsonpath.JsonPath;
import ru.council.metan.jsonpath.JsonPathElement;
import ru.council.metan.merger.actions.*;
import ru.council.metan.merger.conditional.Conditional;
import ru.council.metan.merger.conditional.Either;
import ru.council.metan.merger.conditional.Then;
import ru.council.metan.models.MetaJson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class JsonMerger {

    public static void processFusion(@NonNull Synthesis synthesis, MetaJson totalResult, MetaJson currentResultSetJson) {
        if (synthesis.getOn() != null) {
            processFusion(synthesis.getOn(), totalResult, currentResultSetJson);
        } else {
            throw new IllegalStateException("");
        }
    }

    private static void processFusion(@NonNull String on, MetaJson totalResult, MetaJson currentResultSetJson) {
        /*
            if total result is array
         */
        if (totalResult.isArray()) {
            /*
            Iterating total result array
             */
            boolean notFused = true;
            for (MetaJson totalResultElement : totalResult.getArray()) {
                /*
                Must be node for fusion to be applied
                 */
                Map<String, MetaJson> targetNode = totalResultElement.getNode();
                /*
                Getting keys
                 */
                Set<String> keys = targetNode.keySet();
                /*
                Getting key to fuse on. It's like x for some function
                 */
                MetaJson resolvedTotal = totalResultElement.resolve(on);
                /*
                User must provide valid key.
                 */
                if (resolvedTotal == null) {
                    throw new IllegalStateException("");
                }
                /*
                If current result is also an array
                 */
                // As can be proved. If current row result is an array. It MUST contain only 1 element
                MetaJson currentResultJsonElement = currentResultSetJson.getArray().get(0);
                    /*
                    Getting key
                     */
                MetaJson current = currentResultJsonElement.resolve(on);
                    /*
                    Same validation
                     */
                if (current == null) {
                    throw new IllegalStateException("");
                }
                    /*
                    Check for equality
                     */
                if (current.isScalar() && resolvedTotal.isScalar()) {
                    if (current.getScalar().equals(resolvedTotal.getScalar())) {
                        notFused = false;
                            /*
                            This also must be a node
                             */
                        Map<String, MetaJson> currentNode = currentResultJsonElement.getNode();
                        // Fuse
                        for (String key : keys) {
                            if (currentNode.containsKey(key) && currentNode.get(key).isArray()) {
                                MetaJson targetElement = targetNode.get(key);
                                for (MetaJson newBie : currentNode.get(key).getArray()) {
                                    targetElement.add(newBie);
                                }
                            }
                        }
                    }
                }
            }
            if (notFused) {
                if (currentResultSetJson.isArray()) {
                    totalResult.add(currentResultSetJson.getArray().get(0));
                } else {
                    totalResult.add(currentResultSetJson);
                }
            }
        } else {
            throw new IllegalStateException("Attempt to use fusion with 'Node' root! Use merger instead");
        }
    }

    public static void mergeJsons(@NonNull Merger merger, @NonNull MetaJson parent, @NonNull MetaJson current) {
        log.debug("Processing merger configuration");
        JsonPath parentPath = new JsonPath();
        JsonPath currentPath = new JsonPath();
        List<Action> listOfActions = new ArrayList<>();
        if (merger.getEach() != null) {
            processEach(merger.getEach(), parent, current, listOfActions, parentPath, currentPath);
        } else if (merger.getResolve() != null) {
            processResolve(merger.getResolve(), parent, current, listOfActions, parentPath, currentPath);
        } else if (merger.getAppend() != null) {
            processAppend(merger.getAppend(), listOfActions, parentPath, currentPath);
        } else if (merger.getPut() != null) {
            processPut(merger.getPut(), listOfActions, parentPath, currentPath);
        }

        log.debug("Parent: {}", parent);
        log.debug("Current: {}", current);

        for (Action action : listOfActions) {
            log.debug("Action: {}", action.getType());
            log.debug("Source: {}", action.getSourceJsonPath());
            log.debug("Target: {}", action.getTargetJsonPath());
            action.apply(parent, current);
        }
    }

    public static Boolean processEach(@NonNull Each each, @NonNull MetaJson parent, @NonNull MetaJson current, List<Action> listOfActions, JsonPath parentPath, JsonPath currentPath) {
        log.debug("Processing 'each' configuration");
        log.debug("Each {}", each);
        log.debug("Each conditional: {}", each.getConditional());
        Boolean result = each.getControlFlow() == null ? false : null;
        if (each.getParent()) {
            for (int parentIndex = 0; parentIndex < parent.getArray().size(); parentIndex++) {
                JsonPath parentInnerJsonPath = parentPath.copyWithElement(JsonPathElement.getArraySubscriptionTypeIdentifier(parentIndex));
                MetaJson eachParent = parent.getArray().get(parentIndex);
                if (each.getCurrent()) {
                    for (int currentIndex = 0; currentIndex < current.getArray().size(); currentIndex++) {
                        JsonPath currentInnerJsonPath = currentPath.copyWithElement(JsonPathElement.getArraySubscriptionTypeIdentifier(currentIndex));
                        MetaJson eachCurrent = current.getArray().get(currentIndex);
                        result = processEachSource(each, eachParent, listOfActions, parentInnerJsonPath, currentInnerJsonPath, eachCurrent);
                    }
                } else {
                    result = processEachSource(each, eachParent, listOfActions, parentInnerJsonPath, currentPath, current);
                }
            }
            return result;
        } else if (each.getCurrent()) {
            for (int currentIndex = 0; currentIndex < current.getArray().size(); currentIndex++) {
                log.debug("Checking {} element", currentIndex + 1);
                JsonPath currentInnerJsonPath = currentPath.copyWithElement(JsonPathElement.getArraySubscriptionTypeIdentifier(currentIndex));
                MetaJson eachCurrent = current.getArray().get(currentIndex);
                result = processEachSource(each, parent, listOfActions, parentPath, currentInnerJsonPath, eachCurrent);
                log.debug("Result was: {}", result);
            }
            log.debug("Returning: {}", result);
            return result;
        } else {
            // Useless iter
            throw new IllegalStateException("Empty each");
        }
    }

    private static Boolean processEachSource(@NonNull Each each, @NonNull MetaJson parent, List<Action> listOfActions, JsonPath parentPath, JsonPath currentInnerJsonPath, MetaJson eachCurrent) {
        Boolean result = false;
        log.debug("Each conditional: {}", each.getConditional());
        if (each.getControlFlow() != null) {
            processControlFlow(
                    each.getControlFlow(),
                    parent,
                    eachCurrent,
                    listOfActions,
                    parentPath,
                    currentInnerJsonPath
            );
            log.debug("Returning null because of control flow");
            return null;
        } else if (each.getConditional() != null){
            log.debug("Processing 'conditional'");
            result = processConditional(
                    each.getConditional(),
                    parent,
                    eachCurrent,
                    listOfActions,
                    parentPath,
                    currentInnerJsonPath
            );
            log.debug("Result became: {}", result);
        } else if (each.getPut() != null) {
            processPut(each.getPut(), listOfActions, parentPath, currentInnerJsonPath);
            result = true;
        } else if (each.getAppend() != null) {
            processAppend(each.getAppend(), listOfActions, parentPath, currentInnerJsonPath);
            result = true;
        }
        return result;
    }

    public static void processControlFlow(@NonNull ControlFlow controlFlow, @NonNull MetaJson parent, @NonNull MetaJson current, List<Action> listOfActions, JsonPath parentPath, JsonPath currentPath) {
        log.debug("Processing 'control-flow' configuration");
        if (controlFlow.getEach() != null) {
            Boolean processEach = processEach(controlFlow.getEach(), parent, current, listOfActions, parentPath, currentPath);
            if (processEach != null && !processEach && controlFlow.getEither() != null) {
                log.debug("Each returned false");
                processEither(controlFlow.getEither(), parent, current, listOfActions, parentPath, currentPath);
            }
        } else if (controlFlow.getConditional() != null) {
            Boolean processConditional = processConditional(controlFlow.getConditional(), parent, current, listOfActions, parentPath, currentPath);
            if (processConditional != null && !processConditional && controlFlow.getEither() != null) {
                log.debug("Conditional returned false");
                processEither(controlFlow.getEither(), parent, current, listOfActions, parentPath, currentPath);
            }
        } else {
            // No action for control flow
            throw new IllegalStateException("");
        }
    }

    public static Boolean processConditional(@NonNull Conditional conditional, @NonNull MetaJson parent, @NonNull MetaJson current, List<Action> listOfActions, JsonPath parentPath, JsonPath currentPath) {
        log.debug("Processing 'conditional' configuration");
        if (conditional.getCurrent() != null && conditional.getParent() != null) {
            return processConditional(conditional, parent, current, conditional.getParent(), conditional.getCurrent(), listOfActions, parentPath, currentPath);
        } else if (conditional.getOn() != null) {
            return processConditional(conditional, parent, current, conditional.getOn(), conditional.getOn(), listOfActions, parentPath, currentPath);
        } else {
            throw new IllegalStateException("");
        }
    }

    public static Boolean processThen(@NonNull Then then, MetaJson parent, @NonNull MetaJson current, List<Action> listOfActions, JsonPath parentPath, JsonPath currentPath) {
        log.debug("Processing 'then' configuration!");
        return processAction(parent, current, then.getAppend(), then.getPut(), then.getEach(), then.getResolve(), listOfActions, parentPath, currentPath);
    }

    public static Boolean processEither(@NonNull Either either, @NonNull MetaJson parent, @NonNull MetaJson current, List<Action> listOfActions, JsonPath parentPath, JsonPath currentPath) {
        log.debug("Processing 'either' configuration!");
        return processAction(parent, current, either.getAppend(), either.getPut(), either.getEach(), either.getResolve(), listOfActions, parentPath, currentPath);
    }

    public static void processAppend(@NonNull Append append, List<Action> listOfActions, JsonPath parentPath, JsonPath currentPath) {
        log.debug("Processing 'append' configuration");
        String source = append.getSource();
        String target = append.getTarget();

        Action appendAction = new Action(ActionType.APPEND, parentPath.copyWithPath(target), currentPath.copyWithPath(source));
        listOfActions.add(appendAction);
    }

    public static void processPut(@NonNull Put put, List<Action> listOfActions, JsonPath parentPath, JsonPath currentPath) {
        log.debug("Processing 'put' configuration");
        String source = put.getSource();
        String target = put.getTarget();

        if (source != null && target != null) {
            Action putAction = new Action(ActionType.PUT, parentPath.copyWithPath(target), currentPath.copyWithPath(source));
            listOfActions.add(putAction);
        } else {
            throw new IllegalStateException("");
        }
    }

    private static Boolean processAction(@NonNull MetaJson parent, @NonNull MetaJson current, Append append, Put put, Each each, Resolve resolve, List<Action> listOfActions, JsonPath parentPath, JsonPath currentPath) {
        log.debug("Processing action!");
        log.debug("Parent: {}", parent);
        log.debug("Current: {}", current);
        log.debug("Append: {}", append);
        log.debug("Put: {}", put);
        log.debug("Each: {}", each);
        log.debug("Resolve: {}", resolve);
        log.debug("ParentPath: {}", parentPath.getPath());
        log.debug("CurrentPath: {}", currentPath.getPath());
        if (append != null) {
            processAppend(append, listOfActions, parentPath, currentPath);
            return true;
        } else if (put != null) {
            processPut(put, listOfActions, parentPath, currentPath);
            return true;
        } else if (each != null) {
            return processEach(each, parent, current, listOfActions, parentPath, currentPath);
        } else if (resolve != null) {
            return processResolve(resolve, parent, current, listOfActions, parentPath, currentPath);
        } else {
            throw new IllegalStateException("No action specified!");
        }
    }

    private static Boolean processResolve(@NonNull Resolve resolve, @NonNull MetaJson parent, @NonNull MetaJson current, List<Action> listOfActions, JsonPath parentPath, JsonPath currentPath) {
        return processThen(resolve.getThen(), parent.resolve(resolve.getParent()), current.resolve(resolve.getCurrent()), listOfActions, parentPath.copyWithElement(resolve.getParent()), currentPath.copyWithElement(resolve.getCurrent()));
    }

    private static Boolean processConditional(@NonNull Conditional conditional, @NonNull MetaJson parent, @NonNull MetaJson current, String parentKey, String currentKey, List<Action> listOfActions, JsonPath parentPath, JsonPath currentPath) {
        MetaJson parentCheckJson = parent.getNode().get(parentKey);
        MetaJson currentCheckJson = current.getNode().get(currentKey);
        log.debug("Conditional: {}", conditional);
        log.debug("Parent check json: {}", parentCheckJson);
        log.debug("Current check json: {}", currentCheckJson);
        if (parentCheckJson == null) {
            if (parent.getNode().containsKey(parentKey)) {
                if (currentCheckJson == null && current.getNode().containsKey(currentKey)) {
                    if (conditional.getThen() != null) {
                        return processThen(conditional.getThen(), parent, current, listOfActions, parentPath, currentPath);
                    }
                }
            }
            return false;
        }
        if (currentCheckJson != null && parentCheckJson.getScalar().equals(currentCheckJson.getScalar())) {
            log.debug("Equality!");
            if (conditional.getThen() != null) {
                log.debug("Then not null!");
                return processThen(conditional.getThen(), parent, current, listOfActions, parentPath, currentPath);
            } else {
                return false;
            }
        } else {
            log.debug("Non-equality!");
            if (conditional.getEither() != null) {
                log.debug("Either not null!");
                return processEither(conditional.getEither(), parent, current, listOfActions, parentPath, currentPath);
            } else {
                return false;
            }
        }
    }

}
