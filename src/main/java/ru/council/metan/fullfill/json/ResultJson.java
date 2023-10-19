package ru.council.metan.fullfill.json;

import lombok.*;
import ru.council.metan.fullfill.json.array.Array;
import ru.council.metan.fullfill.json.node.Node;
import ru.council.metan.interfaces.Processable;
import ru.council.metan.merger.actions.Synthesis;
import ru.council.metan.models.MetaJson;

import javax.xml.bind.annotation.*;

@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "json")
@Setter
@ToString
public class ResultJson {

    @XmlElement(name = "synthesis")
    @Getter
    private Synthesis synthesis = null;

    @XmlElement(name = "array")
    private Array array = null;

    @XmlElement(name = "node")
    private Node node = null;

    public Processable<?> getRoot() {
        if (array != null && node == null) {
            return array;
        } else if (node != null && array == null) {
            return node;
        }
        throw new IllegalStateException("Result json must be either array or node");
    }

    public MetaJson getEmptyShape(boolean withInnerFields) {
        return getRoot().getEmptyShape(withInnerFields);
    }

}
