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
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Arrays.asList;

public interface Jrql
{
    interface Transform<T>
    {
        default T map(Star star) { return null; }
        default T map(Query query) { return null; }
        default T map(VariableAssignment variableAssignment) { return null; }
        default T map(Variable variable) { return null; }
        default T map(Subject subject) { return null; }
        default T map(Name name) { return null; }
        default T map(Literal literal) { return null; }
        default T map(InlineFilter inlineFilter) { return null; }
        default T map(Group group) { return null; }
    }

    interface Visitor
    {
        default void visit(Star star) {}
        default void visit(Query query) {}
        default void visit(VariableAssignment variableAssignment) {}
        default void visit(Variable variable) {}
        default void visit(Subject subject) {}
        default void visit(Name name) {}
        default void visit(Literal literal) {}
        default void visit(InlineFilter inlineFilter) {}
        default void visit(Group group) {}
    }

    void accept(Visitor visitor);

    static <T> T map(Jrql jrql, Transform<T> transform)
    {
        if (jrql == null)
            return null;

        final AtomicReference<T> ref = new AtomicReference<>();
        jrql.accept(new Visitor()
        {
            @Override
            public void visit(Star star)
            {
                ref.set(transform.map(star));
            }

            @Override
            public void visit(Query query)
            {
                ref.set(transform.map(query));
            }

            @Override
            public void visit(VariableAssignment variableAssignment)
            {
                ref.set(transform.map(variableAssignment));
            }

            @Override
            public void visit(Variable variable)
            {
                ref.set(transform.map(variable));
            }

            @Override
            public void visit(Subject subject)
            {
                ref.set(transform.map(subject));
            }

            @Override
            public void visit(Name name)
            {
                ref.set(transform.map(name));
            }

            @Override
            public void visit(Literal literal)
            {
                ref.set(transform.map(literal));
            }

            @Override
            public void visit(InlineFilter inlineFilter)
            {
                ref.set(transform.map(inlineFilter));
            }

            @Override
            public void visit(Group group)
            {
                ref.set(transform.map(group));
            }
        });
        return ref.get();
    }

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
