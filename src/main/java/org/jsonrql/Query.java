/*
 * Copyright (c) George Svarovsky 2019. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package org.jsonrql;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static com.fasterxml.jackson.annotation.JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY;
import static com.fasterxml.jackson.annotation.JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

@JsonDeserialize
public final class Query extends Pattern
{
    private final List<Result> select;
    private final List<Result> distinct;
    private final List<Id> describe;
    private final List<Subject> construct;
    private final List<Subject> insert;
    private final List<Subject> delete;
    private final List<Pattern> where;
    private final List<Expression> orderBy;
    private final Integer limit, offset;

    @SafeVarargs
    public final Query context(Consumer<Map<String, Object>>... modify)
    {
        return context(contextWith(modify));
    }

    @Override
    public Query context(Map<String, Object> context)
    {
        return new Query(context, select, distinct, describe, construct, insert, delete, where, orderBy, limit, offset);
    }

    public static Query select(Result... select)
    {
        return select(asList(select));
    }

    public static Query select(List<Result> select)
    {
        return new Query(null, select, null, null, null, null, null, emptyList(), null, null, null);
    }

    public static Query select(String... select)
    {
        return select(stream(select).map(Result::result).collect(toList()));
    }

    @JsonIgnore
    public Optional<List<Result>> select()
    {
        return Optional.ofNullable(select);
    }

    public static Query distinct(Result... distinct)
    {
        return distinct(asList(distinct));
    }

    public static Query distinct(List<Result> distinct)
    {
        return new Query(null, null, distinct, null, null, null, null, emptyList(), null, null, null);
    }

    public static Query distinct(String... distinct)
    {
        return distinct(stream(distinct).map(Result::result).collect(toList()));
    }

    @JsonIgnore
    public Optional<List<Result>> distinct()
    {
        return Optional.ofNullable(distinct);
    }

    public static Query describe(Id... describe)
    {
        return new Query(null, null, null, asList(describe), null, null, null, emptyList(), null, null, null);
    }

    @JsonIgnore
    public Optional<List<Id>> describe()
    {
        return Optional.ofNullable(describe);
    }

    public static Query construct(Subject... construct)
    {
        return construct(asList(construct));
    }

    public static Query construct(List<Subject> construct)
    {
        return new Query(null, null, null, null, construct, null, null, emptyList(), null, null, null);
    }

    @JsonIgnore
    public Optional<List<Subject>> construct()
    {
        return Optional.ofNullable(construct);
    }

    public static Query insert(Subject... insert)
    {
        return insert(asList(insert));
    }

    public static Query insert(List<Subject> insert)
    {
        return new Query(null, null, null, null, null, insert, null, emptyList(), null, null, null);
    }

    @JsonIgnore
    public Optional<List<Subject>> insert()
    {
        return Optional.ofNullable(insert);
    }

    public static Query delete(Subject... delete)
    {
        return delete(asList(delete));
    }

    public static Query delete(List<Subject> delete)
    {
        return new Query(null, null, null, null, null, null, delete, emptyList(), null, null, null);
    }

    @JsonIgnore
    public Optional<List<Subject>> delete()
    {
        return Optional.ofNullable(delete);
    }

    public Query where(Pattern... where)
    {
        return where(asList(where));
    }

    public Query where(List<Pattern> where)
    {
        return new Query(context(), select, distinct, describe, construct, insert, delete, where, orderBy, limit, offset);
    }

    @JsonIgnore
    public Optional<List<Expression>> orderBy()
    {
        return Optional.ofNullable(orderBy);
    }

    public Query orderBy(Expression... orderBy)
    {
        return orderBy(asList(orderBy));
    }

    public Query orderBy(List<Expression> orderBy)
    {
        return new Query(context(), select, distinct, describe, construct, insert, delete, where, orderBy, limit, offset);
    }

    public Query orderBy(String... orderBy)
    {
        return orderBy(stream(orderBy).map(Expression::expression).collect(toList()));
    }

    @JsonIgnore
    public Optional<Integer> limit()
    {
        return Optional.ofNullable(limit);
    }

    public Query limit(int limit)
    {
        return new Query(context(), select, distinct, describe, construct, insert, delete, where, orderBy, limit, offset);
    }

    @JsonIgnore
    public Optional<Integer> offset()
    {
        return Optional.ofNullable(offset);
    }

    public Query offset(int offset)
    {
        return new Query(context(), select, distinct, describe, construct, insert, delete, where, orderBy, limit, offset);
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
        @JsonProperty("@describe") @JsonFormat(with = ACCEPT_SINGLE_VALUE_AS_ARRAY) List<Id> describe,
        @JsonProperty("@construct") @JsonFormat(with = ACCEPT_SINGLE_VALUE_AS_ARRAY) List<Subject> construct,
        @JsonProperty("@insert") @JsonFormat(with = ACCEPT_SINGLE_VALUE_AS_ARRAY) List<Subject> insert,
        @JsonProperty("@delete") @JsonFormat(with = ACCEPT_SINGLE_VALUE_AS_ARRAY) List<Subject> delete,
        @JsonProperty(value = "@where", required = true) @JsonFormat(with = ACCEPT_SINGLE_VALUE_AS_ARRAY) List<Pattern> where,
        @JsonProperty("@orderBy") @JsonFormat(with = ACCEPT_SINGLE_VALUE_AS_ARRAY) List<Expression> orderBy,
        @JsonProperty("@limit") Integer limit,
        @JsonProperty("@offset") Integer offset)
    {
        super(context);
        this.select = select == null ? null : unmodifiableList(select);
        this.distinct = distinct == null ? null : unmodifiableList(distinct);
        this.describe = describe == null ? null : unmodifiableList(describe);
        this.construct = construct == null ? null : unmodifiableList(construct);
        this.insert = insert == null ? null : unmodifiableList(insert);
        this.delete = delete == null ? null : unmodifiableList(delete);
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
    private List<Id> getDescribe()
    {
        return describe;
    }

    @SuppressWarnings("unused")
    @JsonProperty("@construct")
    @JsonFormat(with = WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)
    @JsonInclude(NON_NULL)
    private List<Subject> getConstruct()
    {
        return construct;
    }

    @SuppressWarnings("unused")
    @JsonProperty("@insert")
    @JsonFormat(with = WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)
    @JsonInclude(NON_NULL)
    private List<Subject> getInsert()
    {
        return insert;
    }

    @SuppressWarnings("unused")
    @JsonProperty("@delete")
    @JsonFormat(with = WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)
    @JsonInclude(NON_NULL)
    private List<Subject> getDelete()
    {
        return delete;
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
}
