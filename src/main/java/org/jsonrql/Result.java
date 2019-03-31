/*
 * Copyright (c) George Svarovsky 2019. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package org.jsonrql;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;

import static com.fasterxml.jackson.core.JsonToken.VALUE_STRING;
import static org.jsonrql.Variable.matchVar;

@JsonDeserialize(using = Result.Deserializer.class)
public interface Result extends Jrql
{
    final class Star implements Result
    {
        @Override
        public void accept(Visitor visitor)
        {
            visitor.visit(this);
        }

        @Override
        @JsonValue
        public String toString()
        {
            return "*";
        }
    }
    Result STAR = new Star();

    class Deserializer extends Jrql.Deserializer<Result>
    {
        @Override
        public Result deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
        {
            switch (p.getCurrentToken())
            {
                case VALUE_STRING:
                    return Result.result(p.getText());

                case START_OBJECT:
                    // TODO: Variable assignments, or other object expressions

                default:
                    throw badToken(p, VALUE_STRING);
            }
        }
    }

    static Result result(String str)
    {
        return STAR.toString().equals(str) ? STAR : matchVar(str).orElseThrow(IllegalArgumentException::new);
    }
}
