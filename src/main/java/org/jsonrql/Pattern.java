package org.jsonrql;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;

import static com.fasterxml.jackson.core.JsonToken.START_OBJECT;

@JsonDeserialize(using = Pattern.Deserializer.class)
public interface Pattern extends Jrql
{
    class Deserializer extends Jrql.Deserializer<Pattern>
    {
        @Override
        public Pattern deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
        {
            switch (p.getCurrentToken())
            {
                case START_OBJECT:
                    // Unfortunately we need to read ahead to decide on the target type
                    return readAhead(p, ctxt, node ->
                        fieldsOf(node).anyMatch(Query.CLAUSES::contains) ? Query.class : PatternObject.class);

                default:
                    throw badToken(p, START_OBJECT);
            }
        }
    }
}
