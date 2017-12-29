package org.jsonrql;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;

import static com.fasterxml.jackson.core.JsonToken.START_OBJECT;
import static org.jsonrql.Keywords.KEYWORDS;

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
                        fieldsOf(node).anyMatch(KEYWORDS.clauses::containsKey) ? Query.class
                            : fieldsOf(node).anyMatch(KEYWORDS.groupPatterns::containsKey) ? Group.class
                            : Subject.class);

                default:
                    throw badToken(p, START_OBJECT);
            }
        }
    }
}
