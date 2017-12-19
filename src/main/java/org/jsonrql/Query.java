package org.jsonrql;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY;
import static com.fasterxml.jackson.annotation.JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED;

public class Query
{
    private final List<VariableExpression> select;
    private final List<Map> where;

    public Query(
        @JsonFormat(with = ACCEPT_SINGLE_VALUE_AS_ARRAY) @JsonProperty("@select") List<VariableExpression> select,
        @JsonFormat(with = ACCEPT_SINGLE_VALUE_AS_ARRAY) @JsonProperty("@where") List<Map> where)
    {
        this.select = select;
        this.where = where;
    }

    @JsonProperty("@select")
    @JsonFormat(with = WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)
    public List<VariableExpression> select()
    {
        return select;
    }

    @JsonProperty("@where")
    @JsonFormat(with = WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)
    public List<Map> where()
    {
        return where;
    }
}
