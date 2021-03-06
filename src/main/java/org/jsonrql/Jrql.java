/*
 * Copyright (c) George Svarovsky 2019. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

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
        default T map(Jrql jrql) { return null; }
        default T map(Star star) { return map((Jrql)star); }
        default T map(Query query) { return map((Jrql)query); }
        default T map(Read read) { return map((Query)read); }
        default T map(Construct construct) { return map((Read)construct); }
        default T map(Describe describe) { return map((Read)describe); }
        default T map(Distinct distinct) { return map((Read)distinct); }
        default T map(Select select) { return map((Read)select); }
        default T map(Update update) { return map((Query)update); }
        default T map(VariableAssignment variableAssignment) { return map((Jrql)variableAssignment); }
        default T map(Variable variable) { return map((Jrql)variable); }
        default T map(Subject subject) { return map((Jrql)subject); }
        default T map(Name name) { return map((Jrql)name); }
        default T map(Literal literal) { return map((Jrql)literal); }
        default T map(InlineFilter inlineFilter) { return map((Jrql)inlineFilter); }
        default T map(Group group) { return map((Jrql)group); }
    }

    interface Visitor
    {
        default void visit(Jrql jrql) {}
        default void visit(Star star) { visit((Jrql)star); }
        default void visit(Query query) { visit((Jrql)query); }
        default void visit(Read read) { visit((Query)read); }
        default void visit(Construct construct) { visit((Read)construct); }
        default void visit(Describe describe) { visit((Read)describe); }
        default void visit(Distinct distinct) { visit((Read)distinct); }
        default void visit(Select select) { visit((Read)select); }
        default void visit(Update update) { visit((Query)update); }
        default void visit(VariableAssignment variableAssignment) { visit((Jrql)variableAssignment); }
        default void visit(Variable variable) { visit((Jrql)variable); }
        default void visit(Subject subject) { visit((Jrql)subject); }
        default void visit(Name name) { visit((Jrql)name); }
        default void visit(Literal literal) { visit((Jrql)literal); }
        default void visit(InlineFilter inlineFilter) { visit((Jrql)inlineFilter); }
        default void visit(Group group) { visit((Jrql)group); }
    }

    void accept(Visitor visitor);

    static <T> T map(Jrql jrql, Transform<T> transform)
    {
        if (jrql == null)
            return null;

        final AtomicReference<T> ref = new AtomicReference<>();
        jrql.accept(new Visitor()
        {
            @Override public void visit(Jrql jrql)
            {
                ref.set(transform.map(jrql));
            }

            @Override public void visit(Star star)
            {
                ref.set(transform.map(star));
            }

            @Override public void visit(Query query)
            {
                ref.set(transform.map(query));
            }

            @Override public void visit(Read read)
            {
                ref.set(transform.map(read));
            }

            @Override public void visit(Construct construct)
            {
                ref.set(transform.map(construct));
            }

            @Override public void visit(Describe describe)
            {
                ref.set(transform.map(describe));
            }

            @Override public void visit(Distinct distinct)
            {
                ref.set(transform.map(distinct));
            }

            @Override public void visit(Select select)
            {
                ref.set(transform.map(select));
            }

            @Override public void visit(Update update)
            {
                ref.set(transform.map(update));
            }

            @Override public void visit(VariableAssignment va)
            {
                ref.set(transform.map(va));
            }

            @Override public void visit(Variable variable)
            {
                ref.set(transform.map(variable));
            }

            @Override public void visit(Subject subject)
            {
                ref.set(transform.map(subject));
            }

            @Override public void visit(Name name)
            {
                ref.set(transform.map(name));
            }

            @Override public void visit(Literal literal)
            {
                ref.set(transform.map(literal));
            }

            @Override public void visit(InlineFilter inlineFilter)
            {
                ref.set(transform.map(inlineFilter));
            }

            @Override public void visit(Group group)
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
            return new JsonMappingException(p, format("Expected one of %s", asList(allowedTokens)));
        }
    }
}
