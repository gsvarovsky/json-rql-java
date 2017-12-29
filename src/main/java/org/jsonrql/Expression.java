package org.jsonrql;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;

import static org.jsonrql.Literal.literal;
import static org.jsonrql.Variable.matchVar;

@JsonDeserialize(using = Expression.Deserializer.class)
public interface Expression extends Jrql
{
    class Deserializer extends Jrql.Deserializer<Expression>
    {
        @Override
        public Expression deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
        {
            switch (p.getCurrentToken())
            {
                case VALUE_STRING:
                    return expression(p.getText());

                case START_OBJECT:
                    // TODO: Operations and functions will go here

                default:
                    return ctxt.readValue(p, Literal.class);
            }
        }
    }

    static Expression expression(String str)
    {
        return matchVar(str).map(Expression.class::cast).orElse(literal(str));
    }
}
