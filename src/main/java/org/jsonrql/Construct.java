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
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

@JsonDeserialize
public class Construct extends Read<Construct>
{
    private final List<Subject> construct;

    public static Construct construct(Subject... construct)
    {
        return construct(asList(construct));
    }

    public static Construct construct(List<Subject> construct)
    {
        return new Construct(null, construct, emptyList(), null, null, null);
    }

    @JsonIgnore
    public List<Subject> construct()
    {
        return construct;
    }

    @Override
    public void accept(Visitor visitor)
    {
        visitor.visit(this);
    }

    @JsonCreator
    private Construct(
        @JsonProperty("@context") Context context,
        @JsonProperty("@construct") @JsonFormat(with = ACCEPT_SINGLE_VALUE_AS_ARRAY) List<Subject> construct,
        @JsonProperty(value = "@where", required = true) @JsonFormat(with = ACCEPT_SINGLE_VALUE_AS_ARRAY) List<Pattern> where,
        @JsonProperty("@orderBy") @JsonFormat(with = ACCEPT_SINGLE_VALUE_AS_ARRAY) List<Expression> orderBy,
        @JsonProperty("@limit") Integer limit,
        @JsonProperty("@offset") Integer offset)
    {
        super(context, where, orderBy, limit, offset);
        this.construct = unmodifiableList(construct);
    }

    @Override protected Construct copyWith(
        Context context, List<Pattern> where, List<Expression> orderBy, Integer limit, Integer offset)
    {
        return new Construct(context, construct, where, orderBy, limit, offset);
    }

    @SuppressWarnings("unused")
    @JsonProperty("@construct")
    @JsonFormat(with = WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)
    @JsonInclude(NON_NULL)
    private List<Subject> getConstruct()
    {
        return construct;
    }
}
