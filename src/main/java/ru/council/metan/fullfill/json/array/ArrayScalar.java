package ru.council.metan.fullfill.json.array;

import lombok.*;
import ru.council.metan.enums.JsonElementType;
import ru.council.metan.interfaces.Processable;
import ru.council.metan.models.MetaJson;
import ru.council.metan.models.Scalar;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.sql.ResultSet;
import java.sql.SQLException;

@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "scalar")
@Setter
@Getter
@ToString
@SuppressWarnings("unchecked")
public class ArrayScalar implements Processable<MetaJson> {

    @XmlAttribute(name = "value")
    private String sourceColumnName;

    @Override
    public MetaJson process(ResultSet rs, int rowNum) throws SQLException {
        Object scalarValue = rs.getObject(sourceColumnName);
        if (scalarValue != null) {
            return new MetaJson(JsonElementType.Scalar, new Scalar<>((Class<Object>) scalarValue.getClass(), scalarValue));
        }
        return new MetaJson(JsonElementType.Scalar, new Scalar<>(null, null));
    }

    @Override
    public MetaJson getEmptyShape(boolean withInner) {
        return null;
    }
}
