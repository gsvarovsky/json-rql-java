/*
 * Copyright (c) George Svarovsky 2019. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package org.jsonrql;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Optional;

import static com.fasterxml.jackson.annotation.JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

public abstract class Read<T extends Read> extends Query<T>
{
    protected final List<Expression> orderBy;
    protected final Integer limit, offset;

    @JsonIgnore
    public Optional<List<Expression>> orderBy()
    {
        return Optional.ofNullable(orderBy);
    }

    public T orderBy(Expression... orderBy)
    {
        return orderBy(asList(orderBy));
    }

    public T orderBy(List<Expression> orderBy)
    {
        return copyWith(context, where, orderBy, limit, offset);
    }

    public T orderBy(String... orderBy)
    {
        return orderBy(stream(orderBy).map(Expression::expression).collect(toList()));
    }

    @JsonIgnore
    public Optional<Integer> limit()
    {
        return Optional.ofNullable(limit);
    }

    public T limit(int limit)
    {
        return copyWith(context, where, orderBy, limit, offset);
    }

    @JsonIgnore
    public Optional<Integer> offset()
    {
        return Optional.ofNullable(offset);
    }

    public T offset(int offset)
    {
        return copyWith(context, where, orderBy, limit, offset);
    }

    @Override
    public void accept(Visitor visitor)
    {
        visitor.visit(this);
    }

    protected Read(Context context, List<Pattern> where, List<Expression> orderBy, Integer limit, Integer offset)
    {
        super(context, where);
        this.orderBy = orderBy == null ? null : unmodifiableList(orderBy);
        this.limit = limit;
        this.offset = offset;
    }

    @Override protected T copyWith(Context context, List<Pattern> where)
    {
        return copyWith(context, where, orderBy, limit, offset);
    }

    protected abstract T copyWith(
        Context context, List<Pattern> where, List<Expression> orderBy, Integer limit, Integer offset);

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
