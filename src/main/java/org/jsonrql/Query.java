/*
 * Copyright (c) George Svarovsky 2019. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package org.jsonrql;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

public abstract class Query<T extends Query> extends Pattern
{
    protected final List<Pattern> where;

    @Override public T context(Context context)
    {
        return copyWith(context, where);
    }

    public T where(Pattern... where)
    {
        return where(asList(where));
    }

    public T where(List<Pattern> where)
    {
        final List<Pattern> newWhere = new ArrayList<>(this.where);
        newWhere.addAll(where);
        return copyWith(context, newWhere);
    }

    @JsonProperty("@where")
    @JsonFormat(with = WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)
    public List<Pattern> where()
    {
        return where;
    }

    protected Query(Context context, List<Pattern> where)
    {
        super(context);
        this.where = unmodifiableList(where);
    }

    protected abstract T copyWith(Context context, List<Pattern> where);

    static Class<? extends Query> decideType(JsonNode node)
    {
        if (node.has("@select"))
            return Select.class;
        else if (node.has("@distinct"))
            return Distinct.class;
        else if (node.has("@describe"))
            return Describe.class;
        else if (node.has("@construct"))
            return Construct.class;
        else if (node.has("@insert") || node.has("@delete"))
            return Update.class;
        else
            throw new IllegalArgumentException(
                "Expected one of [@select, @distinct, @describe, @construct, @insert, @delete]");
    }
}
