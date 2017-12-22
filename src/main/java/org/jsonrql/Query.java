package org.jsonrql;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.jsonldjava.core.Context;

import java.util.*;

import static com.fasterxml.jackson.annotation.JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY;
import static com.fasterxml.jackson.annotation.JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toMap;

@JsonDeserialize
public final class Query implements Pattern
{
    public static Query JRQL = new Query(null, null, null, emptyList());
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

    private final Context context;
    private final List<Result> select;
    private final List<PatternObject> construct;
    private final List<Pattern> where;

    public Query context(Context context)
    {
        return new Query(context, select, construct, where);
    }

    @JsonIgnore
    public Context context()
    {
        return context;
    }

    public Query select(Result... select)
    {
        return new Query(context, asList(select), construct, where);
    }

    @JsonIgnore
    public Optional<List<Result>> select()
    {
        return Optional.ofNullable(select);
    }

    public Query construct(PatternObject... construct)
    {
        return new Query(context, select, asList(construct), where);
    }

    @JsonIgnore
    public Optional<List<PatternObject>> construct()
    {
        return Optional.ofNullable(construct);
    }

    public Query where(Pattern... where)
    {
        return new Query(context, select, construct, asList(where));
    }

    @JsonProperty("@where")
    @JsonFormat(with = WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)
    public List<Pattern> where()
    {
        return where;
    }

    @Override
    public void accept(Visitor visitor)
    {
        visitor.visit(this);
    }

    @JsonCreator
    private Query(
        @JsonProperty("@context") Context context,
        @JsonProperty("@select") @JsonFormat(with = ACCEPT_SINGLE_VALUE_AS_ARRAY) List<Result> select,
        @JsonProperty("@construct") @JsonFormat(with = ACCEPT_SINGLE_VALUE_AS_ARRAY) List<PatternObject> construct,
        @JsonProperty("@where") @JsonFormat(with = ACCEPT_SINGLE_VALUE_AS_ARRAY) List<Pattern> where)
    {
        this.context = context == null ? new Context() : new Context(context);
        this.select = select == null ? null : unmodifiableList(select);
        this.construct = construct == null ? construct : unmodifiableList(construct);
        this.where = unmodifiableList(where);
    }

    @SuppressWarnings("unused")
    @JsonProperty("@context")
    @JsonInclude(NON_EMPTY)
    private Map<String, Object> getContext()
    {
        // Ignore an empty @base
        return context.entrySet().stream()
            .filter(e -> !"@base".equals(e.getKey()) || !"".equals(e.getValue()))
            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @SuppressWarnings("unused")
    @JsonProperty("@select")
    @JsonFormat(with = WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)
    @JsonInclude(NON_NULL)
    private List<Result> getSelect()
    {
        return select;
    }

    @SuppressWarnings("unused")
    @JsonProperty("@construct")
    @JsonFormat(with = WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)
    @JsonInclude(NON_NULL)
    private List<PatternObject> getConstruct()
    {
        return construct;
    }
}
