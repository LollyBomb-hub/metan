package ru.council.metan.interfaces;

import com.fasterxml.jackson.core.JsonProcessingException;
import ru.council.metan.models.MetaJson;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface Processable<T> {

    T process(ResultSet rs, int rowNum) throws SQLException, JsonProcessingException;
    MetaJson getEmptyShape(boolean withInnerFields);

}
