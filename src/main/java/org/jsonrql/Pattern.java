/*
 * Copyright (c) George Svarovsky 2019. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package org.jsonrql;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.jsonldjava.utils.JsonUtils;

import java.io.IOException;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.core.JsonToken.START_ARRAY;
import static com.fasterxml.jackson.core.JsonToken.START_OBJECT;
import static org.jsonrql.Group.group;
import static org.jsonrql.Keywords.KEYWORDS;

@JsonDeserialize(using = Pattern.Deserializer.class)
public abstract class Pattern implements Jrql
{
    protected final Context context;

    public abstract Pattern context(Context context);

    public Context context()
    {
        return context == null ? Context.context() : context;
    }

    Pattern(Context context)
    {
        this.context = context == null || context.isEmpty() ? null : context;
    }

    @JsonProperty("@context")
    @JsonInclude(NON_NULL)
    public Context getContext()
    {
        return context;
    }

    @Override
    public String toString()
    {
        try
        {
            return JsonUtils.toPrettyString(this);
        }
        catch (IOException e)
        {
            throw new AssertionError(e);
        }
    }

    static class Deserializer extends Jrql.Deserializer<Pattern>
    {
        @Override
        public Pattern deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
        {
            switch (p.getCurrentToken())
            {
                case START_OBJECT:
                    // Unfortunately we need to read ahead to decide on the target type
                    return readAhead(p, ctxt, node ->
                        fieldsOf(node).anyMatch(KEYWORDS.clauses::containsKey) ? Query.decideType(node)
                            : fieldsOf(node).anyMatch(KEYWORDS.groupPatterns::containsKey) ? Group.class
                            : Subject.class);

                case START_ARRAY:
                    // Create a group
                    return group(ctxt.readValue(p, Subject[].class));

                default:
                    throw badToken(p, START_OBJECT, START_ARRAY);
            }
        }
    }
}
