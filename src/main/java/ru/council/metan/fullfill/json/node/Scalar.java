package ru.council.metan.fullfill.json.node;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import ru.council.metan.enums.JsonElementType;
import ru.council.metan.interfaces.Processable;
import ru.council.metan.models.MetaJson;

import javax.xml.bind.annotation.XmlAttribute;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.util.UUID;

@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Scalar implements Processable<MetaJson> {

    @XmlAttribute(name = "key", required = true)
    @Getter
    private String accessKey = new UUID(new Random().nextLong(), new Random().nextLong()).toString();

    @XmlAttribute(name = "value", required = true)
    @Getter
    private String sourceColumnName = "";

    @Override
    @SuppressWarnings("unchecked")
    public MetaJson process(ResultSet rs, int rowNum) throws SQLException {
        log.debug("Getting value of column {} in scalar with access key {}", sourceColumnName, accessKey);
        Object scalarValue = rs.getObject(sourceColumnName);
        log.debug("Got {}", scalarValue);
        if (scalarValue != null) {
            return new MetaJson(JsonElementType.Scalar, new ru.council.metan.models.Scalar<>((Class<Object>) scalarValue.getClass(), scalarValue));
        }
        return new MetaJson(JsonElementType.Scalar, new ru.council.metan.models.Scalar<>(null, null));
    }

    @Override
    public MetaJson getEmptyShape(boolean withInner) {
        return null;
    }
}
