package org.jsonrql;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;

import static com.fasterxml.jackson.core.JsonToken.VALUE_STRING;
import static org.jsonrql.Jrql.badToken;
import static org.jsonrql.Variable.matchVar;

@JsonDeserialize(using = Value.Deserializer.class)
public interface Value extends Jrql
{
    class Deserializer extends JsonDeserializer<Value>
    {
        @Override
        public Value deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
        {
            switch (p.getCurrentToken())
            {
                case VALUE_STRING:
                    return from(p.getText());

                default:
                    throw badToken(p, VALUE_STRING);
            }
        }
    }

    Object asJsonLd();

    static Value from(String text)
    {
        // Note that JSON-LD has ways of being explicit about @ids and @values
        return matchVar(text).map(Value.class::cast).orElse(new Text(text));
    }
}
