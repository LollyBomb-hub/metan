package ru.council.metan.fullfill.json.array;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import ru.council.metan.enums.JsonElementType;
import ru.council.metan.exceptions.ProcessingException;
import ru.council.metan.interfaces.Processable;
import ru.council.metan.models.MetaJson;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class Array implements Processable<MetaJson> {

    @XmlAttribute(name = "key")
    private String accessKey;

    @XmlElement(name = "node")
    private ArrayNode node = null;

    @XmlElement(name = "scalar")
    private ArrayScalar scalar = null;

    public boolean isSimple() {
        return scalar != null && node == null;
    }

    public boolean isComplex() {
        return scalar == null && node != null;
    }

    @Override
    public MetaJson process(ResultSet rs, int rowNum) throws SQLException, JsonProcessingException {
        log.debug("Processing array with access key {}", getAccessKey());

        MetaJson result = new MetaJson(JsonElementType.Array, new ArrayList<MetaJson>());
        if (isSimple()) {
            result.add(scalar.process(rs, rowNum));
            return result;
        } else if (isComplex()) {
            result.add(node.process(rs, rowNum));
            return result;
        } else {
            throw new ProcessingException("Invalid array item configuration!");
        }
    }

    @Override
    public MetaJson getEmptyShape(boolean withInner) {
        MetaJson result = new MetaJson(JsonElementType.Array);

        if (isComplex() && withInner) {
            result.add(node.getEmptyShape(true));
        }

        return result;
    }
}
