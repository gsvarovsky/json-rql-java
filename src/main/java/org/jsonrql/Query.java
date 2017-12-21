package org.jsonrql;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.fasterxml.jackson.annotation.JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY;
import static com.fasterxml.jackson.annotation.JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

@JsonDeserialize
public final class Query implements Pattern
{
    public static Query JRQL = new Query(null, emptyList());
    public static Set<String> CLAUSES = new HashSet<>(asList(
        "@construct",
        "@select",
        "@describe",
        "@distinct",
        "@where",
        "@orderBy",
        "@groupBy",
        "@having",
        "@limit",
        "@offset",
        "@values"
    ));

    private final List<Result> select;
    private final List<Pattern> where;

    public Query select(Result... select)
    {
        return new Query(asList(select), where);
    }

    public Query where(Pattern... where)
    {
        return new Query(select, asList(where));
    }

    @JsonCreator
    private Query(
        @JsonFormat(with = ACCEPT_SINGLE_VALUE_AS_ARRAY) @JsonProperty("@select") List<Result> select,
        @JsonFormat(with = ACCEPT_SINGLE_VALUE_AS_ARRAY) @JsonProperty("@where") List<Pattern> where)
    {
        this.select = select == null ? null : unmodifiableList(select);
        this.where = unmodifiableList(where);
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

    @SuppressWarnings("unused")
    @JsonProperty("@select")
    @JsonFormat(with = WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)
    @JsonInclude(NON_NULL)
    private List<Result> getSelect()
    {
        return select;
    }

    @JsonProperty("@where")
    @JsonFormat(with = WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)
    public List<Pattern> where()
    {
        return where;
    }
}
