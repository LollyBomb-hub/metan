package ru.council.metan.jdbc;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import ru.council.metan.merger.JsonMerger;
import ru.council.metan.fullfill.json.ResultJson;
import ru.council.metan.fullfill.json.array.Array;
import ru.council.metan.fullfill.json.node.Node;
import ru.council.metan.interfaces.Processable;
import ru.council.metan.merger.Merger;
import ru.council.metan.models.MetaJson;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MetaJsonRowMapper implements RowMapper<MetaJson> {

    private final ResultJson specification;
    @Getter
    private MetaJson json = null;
    @Getter
    private List<MetaJson> listOfJsons = null;

    public MetaJsonRowMapper(ResultJson specification) {
        this.specification = specification;
    }

    /**
     * Returns rough filled form specified in result json
     *
     * @param rs     result set. Spring data ref
     * @param rowNum row number
     * @return meta json if method could return or null if it is node and query returned multiple rows
     */
    @SneakyThrows
    public MetaJson mapRow(@NonNull ResultSet rs, int rowNum) {
        Processable<?> root = specification.getRoot();

        if (root instanceof Node) {
            log.debug("Root element is node");
            MetaJson currentLineProcessResult = ((Node) root).process(rs, rowNum);

            if (json != null) {
                log.warn("Configuration uses node as root element but provides multiple result set!");
                if (listOfJsons == null) {
                    listOfJsons = new ArrayList<>();
                    listOfJsons.add(json);
                    json = null;
                }
                listOfJsons.add(currentLineProcessResult);
            } else {
                json = currentLineProcessResult;
            }
        } else if (root instanceof Array) {
            log.debug("Root element is array");
            MetaJson current = ((Array) root).process(rs, rowNum);

            if (json == null) {
                json = current;
            } else if (json.isArray()) {
                if (specification.getSynthesis() != null) {
                    JsonMerger.processFusion(specification.getSynthesis(), json, current);
                } else {
                    json.getArray().addAll(current.getArray());
                }
            }
        }

        return json;
    }

    public MetaJson getJson(Merger merger) {
        MetaJson result = specification.getEmptyShape(json == null);
        if (json == null) {
            for (MetaJson oneOfResult : listOfJsons) {
                JsonMerger.mergeJsons(merger, result, oneOfResult);
            }
            return result;
        } else {
            if (merger.getSupplyParent()) {
                JsonMerger.mergeJsons(merger, json, json);
            } else {
                JsonMerger.mergeJsons(merger, result, json);
            }
        }
        if (merger.getSupplyParent()) {
            return json;
        }
        return result;
    }

}
