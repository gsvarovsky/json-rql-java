package org.jsonrql;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.fasterxml.jackson.annotation.JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY;
import static com.fasterxml.jackson.annotation.JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

public final class Query implements Jrql
{
    private final List<Result> select;
    private final List<Map> where;

    public Query(
        @JsonFormat(with = ACCEPT_SINGLE_VALUE_AS_ARRAY) @JsonProperty("@select") List<Result> select,
        @JsonFormat(with = ACCEPT_SINGLE_VALUE_AS_ARRAY) @JsonProperty("@where") List<Map> where)
    {
        this.select = select;
        this.where = where;
    }

    @Override
    public void accept(Visitor visitor)
    {
        visitor.visit(this);
    }

    @JsonIgnore
    public Optional<List<Result>> select()
    {
        return Optional.ofNullable(select);
    }

    @JsonProperty("@select")
    @JsonFormat(with = WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)
    @JsonInclude(NON_NULL)
    protected List<Result> getSelect()
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
