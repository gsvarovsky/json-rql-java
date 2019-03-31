/*
 * Copyright (c) George Svarovsky 2019. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package org.jsonrql;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY;
import static com.fasterxml.jackson.annotation.JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableMap;
import static org.jsonrql.Keywords.KEYWORDS;

@JsonDeserialize
public final class InlineFilter implements Value
{
    private final Variable variable;
    private final Map<String, List<Expression>> filters;

    public static InlineFilter filter(String variable, String operator, Expression... arguments)
    {
        final InlineFilter inlineFilter = new InlineFilter(
            Variable.matchVar(variable).orElseThrow(IllegalArgumentException::new));
        inlineFilter.setFilter(operator, asList(arguments));
        return inlineFilter;
    }

    @JsonCreator
    private InlineFilter(@JsonProperty("@id") Variable variable)
    {
        this.variable = variable == null ? Variable.generate() : variable;
        this.filters = new HashMap<>();
    }

    @JsonProperty("@id")
    public Variable variable()
    {
        return variable;
    }

    @JsonAnyGetter
    @JsonFormat(with = WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)
    @SuppressWarnings("unused")
    public Map<String, List<Expression>> filters()
    {
        return unmodifiableMap(filters);
    }

    @JsonAnySetter
    @JsonFormat(with = ACCEPT_SINGLE_VALUE_AS_ARRAY)
    @SuppressWarnings("unused")
    private void setFilter(String operator, List<Expression> expressions)
    {
        if (!KEYWORDS.operators.containsKey(operator))
            throw new IllegalArgumentException(format("Unrecognized operator \"%s\"", operator));

        this.filters.put(operator, expressions);
    }

    @Override
    public void accept(Visitor visitor)
    {
        visitor.visit(this);
    }
}
