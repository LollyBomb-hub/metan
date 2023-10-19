package ru.council.metan.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.*;

@AllArgsConstructor
@Getter
@ToString
@SuppressWarnings({"unused", "unchecked"})
public class Scalar<T> {

    @Setter
    @JsonIgnore
    private Class<T> type;
    @JsonValue
    private T value;

    public Scalar(T value) {
        this.value = value;
        this.type = value == null ? null : (Class<T>) value.getClass();
    }

    public void setValue(T value) {
        this.value = value;
        if (value != null)
            this.type = (Class<T>) value.getClass();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Scalar) {
            if (((Scalar<?>) obj).getType() == null && getType() == null) {
                return ((Scalar<?>) obj).getValue() == null && getValue() == null;
            } else {
                if (((Scalar<?>) obj).getType() != null && ((Scalar<?>) obj).getType().equals(getType())) {
                    return ((Scalar<?>) obj).getValue() != null && ((Scalar<?>) obj).getValue().equals(getValue());
                }
            }
        }
        return false;
    }
}
