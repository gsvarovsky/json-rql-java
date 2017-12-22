package org.jsonrql;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import org.jsonrql.Result.Star;

import java.io.IOException;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Arrays.asList;

public interface Jrql
{
    interface Visitor
    {
        default void visit(Star star) {}
        default void visit(Query query) {}
        default void visit(VariableAssignment variableAssignment) {}
        default void visit(Variable variable) {}
        default void visit(PatternObject patternObject) {}
        default void visit(Name name) {}
        default void visit(Literal literal) {}
    }

    void accept(Visitor visitor);

    abstract class Deserializer<T> extends JsonDeserializer<T>
    {
        T readAhead(JsonParser p,
                    DeserializationContext ctxt,
                    Function<JsonNode, Class<? extends T>> decideType) throws IOException
        {
            final JsonNode node = p.readValueAsTree();
            final JsonParser nodeParser = new TreeTraversingParser(node, p.getCodec());
            nodeParser.nextToken();
            return ctxt.readValue(nodeParser, decideType.apply(node));
        }

        static Stream<String> fieldsOf(JsonNode node)
        {
            return Stream.generate(node.fieldNames()::next).limit(node.size());
        }

        static JsonMappingException badToken(JsonParser p, JsonToken... allowedTokens)
        {
            return badMapping(p, "Expected one of %s", asList(allowedTokens));
        }

        static JsonMappingException badMapping(JsonParser p, String msg, Object... args)
        {
            return new JsonMappingException(p, format(msg, args));
        }
    }
}
