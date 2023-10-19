package ru.council.metan.fullfill.json.node;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import ru.council.metan.enums.JsonElementType;
import ru.council.metan.fullfill.json.array.Array;
import ru.council.metan.interfaces.Processable;
import ru.council.metan.models.MetaJson;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@SuppressWarnings("duplicated")
public class Node implements Processable<MetaJson> {

    @XmlAttribute(name = "key")
    private String accessKey;

    @XmlElement(name = "scalar")
    private List<Scalar> scalars = new ArrayList<>();

    @XmlElement(name = "array")
    private List<Array> arrays = new ArrayList<>();

    @XmlElement(name = "node")
    private List<Node> nodes = new ArrayList<>();

    @Override
    public MetaJson process(ResultSet rs, int rowNum) throws SQLException, JsonProcessingException {
        log.debug("Processing node with access key '{}'", getAccessKey());
        MetaJson result = new MetaJson(JsonElementType.Node, new HashMap<>());

        if (scalars != null) {
            for (Scalar scalar : scalars) {
                result.put(scalar.getAccessKey(), scalar.process(rs, rowNum));
            }
        }

        if (nodes != null) {
            for (Node node : nodes) {
                MetaJson childNodeResult = node.process(rs, rowNum);
                result.put(node.getAccessKey(), childNodeResult);
            }
        }

        if (arrays != null) {
            for (Array array : arrays) {
                result.put(array.getAccessKey(), array.process(rs, rowNum));
            }
        }

        return result;
    }

    @Override
    public MetaJson getEmptyShape(boolean withInner) {
        MetaJson result = new MetaJson(JsonElementType.Node);

        if (withInner) {
            if (nodes != null) {
                for (Node node : nodes) {
                    result.put(node.getAccessKey(), node.getEmptyShape(true));
                }
            }

            if (arrays != null) {
                for (Array array : arrays) {
                    result.put(array.getAccessKey(), array.getEmptyShape(true));
                }
            }
        }

        return result;
    }
}
