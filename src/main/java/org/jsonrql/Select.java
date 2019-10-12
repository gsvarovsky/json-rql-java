/*
 * Copyright (c) George Svarovsky 2019. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package org.jsonrql;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY;
import static com.fasterxml.jackson.annotation.JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

@JsonDeserialize
public class Select extends Read<Select>
{
    private final List<Result> select;

    public static Select select(Result... select)
    {
        return select(asList(select));
    }

    public static Select select(List<Result> select)
    {
        return new Select(null, select, emptyList(), null, null, null);
    }

    public static Select select(String... select)
    {
        return select(stream(select).map(Result::result).collect(toList()));
    }

    @JsonIgnore
    public List<Result> select()
    {
        return select;
    }

    @Override
    public void accept(Visitor visitor)
    {
        visitor.visit(this);
    }

    @JsonCreator
    private Select(
        @JsonProperty("@context") Context context,
        @JsonProperty("@select") @JsonFormat(with = ACCEPT_SINGLE_VALUE_AS_ARRAY) List<Result> select,
        @JsonProperty(value = "@where", required = true) @JsonFormat(with = ACCEPT_SINGLE_VALUE_AS_ARRAY) List<Pattern> where,
        @JsonProperty("@orderBy") @JsonFormat(with = ACCEPT_SINGLE_VALUE_AS_ARRAY) List<Expression> orderBy,
        @JsonProperty("@limit") Integer limit,
        @JsonProperty("@offset") Integer offset)
    {
        super(context, where, orderBy, limit, offset);
        this.select = unmodifiableList(select);
    }

    @Override protected Select copyWith(
        Context context, List<Pattern> where, List<Expression> orderBy, Integer limit, Integer offset)
    {
        return new Select(context, select, where, orderBy, limit, offset);
    }

    @SuppressWarnings("unused")
    @JsonProperty("@select")
    @JsonFormat(with = WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)
    @JsonInclude(NON_NULL)
    private List<Result> getSelect()
    {
        return select;
    }
}
