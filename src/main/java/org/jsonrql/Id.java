package org.jsonrql;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;

import static com.fasterxml.jackson.core.JsonToken.FIELD_NAME;
import static com.fasterxml.jackson.core.JsonToken.VALUE_STRING;
import static org.jsonrql.Name.name;
import static org.jsonrql.Variable.matchVar;

@JsonDeserialize(using = Id.Deserializer.class)
public interface Id extends Value, Expression
{
    class Deserializer extends Jrql.Deserializer<Id>
    {
        @Override
        public Id deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
        {
            switch (p.getCurrentToken())
            {
                case FIELD_NAME:
                case VALUE_STRING:
                    return Id.id(p.getText());

                default:
                    throw badToken(p, FIELD_NAME, VALUE_STRING);
            }
        }
    }

    static Id id(String text)
    {
        return matchVar(text).map(Id.class::cast).orElse(name(text));
    }
}
