package org.jsonrql;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.*;

import static com.fasterxml.jackson.annotation.JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY;
import static com.fasterxml.jackson.annotation.JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static java.util.Arrays.asList;
import static java.util.Collections.*;

@JsonDeserialize
public final class Query implements Pattern
{
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

    private final Map<String, Object> context;
    private final List<Result> select;
    private final List<PatternObject> construct;
    private final List<Pattern> where;

    public Query base(String base)
    {
        return withContext("@base", base);
    }

    public Query vocab(String vocab)
    {
        return withContext("@vocab", vocab);
    }

    public Query prefix(String pre, String expanded)
    {
        return withContext(pre, expanded);
    }

    @JsonProperty("@context")
    @JsonInclude(NON_EMPTY)
    public Map<String, Object> context()
    {
        return context;
    }

    public static Query select(Result... select)
    {
        return new Query(null, asList(select), null, emptyList());
    }

    @JsonIgnore
    public Optional<List<Result>> select()
    {
        return Optional.ofNullable(select);
    }

    public static Query construct(PatternObject... construct)
    {
        return new Query(null, null, asList(construct), emptyList());
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
        @JsonProperty("@context") Map<String, Object> context,
        @JsonProperty("@select") @JsonFormat(with = ACCEPT_SINGLE_VALUE_AS_ARRAY) List<Result> select,
        @JsonProperty("@construct") @JsonFormat(with = ACCEPT_SINGLE_VALUE_AS_ARRAY) List<PatternObject> construct,
        @JsonProperty(value = "@where", required = true) @JsonFormat(with = ACCEPT_SINGLE_VALUE_AS_ARRAY) List<Pattern> where)
    {
        this.context = context == null ? emptyMap() : unmodifiableMap(context);
        this.select = select == null ? null : unmodifiableList(select);
        this.construct = construct == null ? null : unmodifiableList(construct);
        this.where = unmodifiableList(where);
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

    private Query withContext(String key, Object value)
    {
        final Map<String, Object> newContext = new HashMap<>(context);
        newContext.put(key, value);
        return new Query(newContext, select, construct, where);
    }
}
