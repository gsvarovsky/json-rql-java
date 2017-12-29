package org.jsonrql;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.fasterxml.jackson.annotation.JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY;
import static com.fasterxml.jackson.annotation.JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.*;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@JsonDeserialize
public final class Query implements Pattern
{
    private final Map<String, Object> context;
    private final List<Result> select;
    private final List<Result> distinct;
    private final List<Result> describe;
    private final List<PatternObject> construct;
    private final List<Pattern> where;
    private final List<Expression> orderBy;
    private final Integer limit, offset;

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

    @JsonIgnore
    public Map<String, String> prefixes()
    {
        return context.entrySet().stream()
            .filter(e -> e.getValue() instanceof String && !"@base".equals(e.getKey()))
            .collect(toMap(
                e -> "@vocab".equals(e.getKey()) ? "" : e.getKey(),
                e -> e.getValue().toString()));
    }

    public static Query select(Result... select)
    {
        return new Query(null, asList(select), null, null, null, emptyList(), null, null, null);
    }

    public static Query select(String... select)
    {
        return new Query(null, stream(select).map(Result::result).collect(toList()), null, null, null, emptyList(),
                         null, null, null);
    }

    @JsonIgnore
    public Optional<List<Result>> select()
    {
        return Optional.ofNullable(select);
    }

    public static Query distinct(Result... distinct)
    {
        return new Query(null, null, asList(distinct), null, null, emptyList(), null, null, null);
    }

    public static Query distinct(String... distinct)
    {
        return new Query(null, null, stream(distinct).map(Result::result).collect(toList()), null, null, emptyList(),
                         null, null, null);
    }

    @JsonIgnore
    public Optional<List<Result>> distinct()
    {
        return Optional.ofNullable(distinct);
    }

    public static Query describe(Result... describe)
    {
        return new Query(null, null, null, asList(describe), null, emptyList(), null, null, null);
    }

    @JsonIgnore
    public Optional<List<Result>> describe()
    {
        return Optional.ofNullable(describe);
    }

    public static Query construct(PatternObject... construct)
    {
        return new Query(null, null, null, null, asList(construct), emptyList(), null, null, null);
    }

    @JsonIgnore
    public Optional<List<PatternObject>> construct()
    {
        return Optional.ofNullable(construct);
    }

    public Query where(Pattern... where)
    {
        return new Query(context, select, distinct, describe, construct, asList(where), orderBy, limit, offset);
    }

    @JsonIgnore
    public Optional<List<Expression>> orderBy()
    {
        return Optional.ofNullable(orderBy);
    }

    public Query orderBy(Expression... orderBy)
    {
        return new Query(context, select, distinct, describe, construct, where, asList(orderBy), limit, offset);
    }

    public Query orderBy(String... orderBy)
    {
        return new Query(context, select, distinct, describe, construct, where,
                         stream(orderBy).map(Expression::expression).collect(toList()), limit, offset);
    }

    @JsonIgnore
    public Optional<Integer> limit()
    {
        return Optional.ofNullable(limit);
    }

    public Query limit(int limit)
    {
        return new Query(context, select, distinct, describe, construct, where, orderBy, limit, offset);
    }

    @JsonIgnore
    public Optional<Integer> offset()
    {
        return Optional.ofNullable(offset);
    }

    public Query offset(int offset)
    {
        return new Query(context, select, distinct, describe, construct, where, orderBy, limit, offset);
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
        @JsonProperty("@distinct") @JsonFormat(with = ACCEPT_SINGLE_VALUE_AS_ARRAY) List<Result> distinct,
        @JsonProperty("@describe") @JsonFormat(with = ACCEPT_SINGLE_VALUE_AS_ARRAY) List<Result> describe,
        @JsonProperty("@construct") @JsonFormat(with = ACCEPT_SINGLE_VALUE_AS_ARRAY) List<PatternObject> construct,
        @JsonProperty(value = "@where", required = true) @JsonFormat(with = ACCEPT_SINGLE_VALUE_AS_ARRAY) List<Pattern> where,
        @JsonProperty("@orderBy") @JsonFormat(with = ACCEPT_SINGLE_VALUE_AS_ARRAY) List<Expression> orderBy,
        @JsonProperty("@limit") Integer limit,
        @JsonProperty("@offset") Integer offset)
    {
        this.context = context == null ? emptyMap() : unmodifiableMap(context);
        this.select = select == null ? null : unmodifiableList(select);
        this.distinct = distinct == null ? null : unmodifiableList(distinct);
        this.describe = describe == null ? null : unmodifiableList(describe);
        this.construct = construct == null ? null : unmodifiableList(construct);
        this.where = unmodifiableList(where);
        this.orderBy = orderBy;
        this.limit = limit;
        this.offset = offset;
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
    @JsonProperty("@distinct")
    @JsonFormat(with = WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)
    @JsonInclude(NON_NULL)
    private List<Result> getDistinct()
    {
        return distinct;
    }

    @SuppressWarnings("unused")
    @JsonProperty("@describe")
    @JsonFormat(with = WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)
    @JsonInclude(NON_NULL)
    private List<Result> getDescribe()
    {
        return describe;
    }

    @SuppressWarnings("unused")
    @JsonProperty("@construct")
    @JsonFormat(with = WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)
    @JsonInclude(NON_NULL)
    private List<PatternObject> getConstruct()
    {
        return construct;
    }

    @SuppressWarnings("unused")
    @JsonProperty("@orderBy")
    @JsonFormat(with = WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)
    @JsonInclude(NON_NULL)
    private List<Expression> getOrderBy()
    {
        return orderBy;
    }

    @SuppressWarnings("unused")
    @JsonProperty("@limit")
    @JsonInclude(NON_NULL)
    private Integer getLimit()
    {
        return limit;
    }

    @SuppressWarnings("unused")
    @JsonProperty("@offset")
    @JsonInclude(NON_NULL)
    private Integer getOffset()
    {
        return offset;
    }

    private Query withContext(String key, Object value)
    {
        final Map<String, Object> newContext = new HashMap<>(context);
        newContext.put(key, value);
        return new Query(newContext, select, distinct, describe, construct, where, orderBy, limit, offset);
    }
}
