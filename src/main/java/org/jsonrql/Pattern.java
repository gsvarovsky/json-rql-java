package org.jsonrql;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;

import java.io.IOException;
import java.util.stream.Stream;

import static com.fasterxml.jackson.core.JsonToken.START_OBJECT;
import static org.jsonrql.Jrql.badToken;

@JsonDeserialize(using = Pattern.Deserializer.class)
public interface Pattern extends Jrql
{
    class Deserializer extends JsonDeserializer<Pattern>
    {
        @Override
        public Pattern deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
        {
            switch (p.getCurrentToken())
            {
                case START_OBJECT:
                    // Unfortunately we need to read ahead to decide on the target type
                    final JsonNode node = p.readValueAsTree();
                    final JsonParser nodeParser = new TreeTraversingParser(node, p.getCodec());
                    return isQuery(node) ?
                        nodeParser.readValueAs(Query.class) :
                        nodeParser.readValueAs(PatternObject.class);

                default:
                    throw badToken(p, START_OBJECT);
            }
        }

        private boolean isQuery(JsonNode node)
        {
            return Stream.generate(node.fieldNames()::next).limit(node.size()).anyMatch(Query.CLAUSES::contains);
        }
    }
}
