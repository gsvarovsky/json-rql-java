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

@JsonDeserialize
public class Describe extends Read<Describe>
{
    private final List<Id> describe;

    public static Describe describe(Id... describe)
    {
        return new Describe(null, asList(describe), emptyList(), null, null, null);
    }

    public static Describe describe(String... describe)
    {
        return describe(stream(describe).map(Id::id).toArray(Id[]::new));
    }

    @JsonIgnore
    public List<Id> describe()
    {
        return describe;
    }

    @Override
    public void accept(Visitor visitor)
    {
        visitor.visit(this);
    }

    @JsonCreator
    private Describe(
        @JsonProperty("@context") Context context,
        @JsonProperty("@describe") @JsonFormat(with = ACCEPT_SINGLE_VALUE_AS_ARRAY) List<Id> describe,
        @JsonProperty(value = "@where", required = true) @JsonFormat(with = ACCEPT_SINGLE_VALUE_AS_ARRAY) List<Pattern> where,
        @JsonProperty("@orderBy") @JsonFormat(with = ACCEPT_SINGLE_VALUE_AS_ARRAY) List<Expression> orderBy,
        @JsonProperty("@limit") Integer limit,
        @JsonProperty("@offset") Integer offset)
    {
        super(context, where, orderBy, limit, offset);
        this.describe = unmodifiableList(describe);
    }

    @Override protected Describe copyWith(
        Context context, List<Pattern> where, List<Expression> orderBy, Integer limit, Integer offset)
    {
        return new Describe(context, describe, where, orderBy, limit, offset);
    }

    @SuppressWarnings("unused")
    @JsonProperty("@describe")
    @JsonFormat(with = WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)
    @JsonInclude(NON_NULL)
    private List<Id> getDescribe()
    {
        return describe;
    }
}
