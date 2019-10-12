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
public class Distinct extends Read<Distinct>
{
    private final List<Result> distinct;

    public static Distinct distinct(Result... distinct)
    {
        return distinct(asList(distinct));
    }

    public static Distinct distinct(List<Result> distinct)
    {
        return new Distinct(null, distinct, emptyList(), null, null, null);
    }

    public static Distinct distinct(String... distinct)
    {
        return distinct(stream(distinct).map(Result::result).collect(toList()));
    }

    @JsonIgnore
    public List<Result> distinct()
    {
        return distinct;
    }

    @Override
    public void accept(Visitor visitor)
    {
        visitor.visit(this);
    }

    @JsonCreator
    private Distinct(
        @JsonProperty("@context") Context context,
        @JsonProperty("@distinct") @JsonFormat(with = ACCEPT_SINGLE_VALUE_AS_ARRAY) List<Result> distinct,
        @JsonProperty(value = "@where", required = true) @JsonFormat(with = ACCEPT_SINGLE_VALUE_AS_ARRAY) List<Pattern> where,
        @JsonProperty("@orderBy") @JsonFormat(with = ACCEPT_SINGLE_VALUE_AS_ARRAY) List<Expression> orderBy,
        @JsonProperty("@limit") Integer limit,
        @JsonProperty("@offset") Integer offset)
    {
        super(context, where, orderBy, limit, offset);
        this.distinct = unmodifiableList(distinct);
    }

    @Override protected Distinct copyWith(
        Context context, List<Pattern> where, List<Expression> orderBy, Integer limit, Integer offset)
    {
        return new Distinct(context, distinct, where, orderBy, limit, offset);
    }

    @SuppressWarnings("unused")
    @JsonProperty("@distinct")
    @JsonFormat(with = WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)
    @JsonInclude(NON_NULL)
    private List<Result> getDistinct()
    {
        return distinct;
    }
}
