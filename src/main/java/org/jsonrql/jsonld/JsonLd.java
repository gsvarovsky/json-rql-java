/*
 * Copyright (c) George Svarovsky 2019. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package org.jsonrql.jsonld;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsonrql.*;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.*;
import static java.util.stream.Collectors.toList;

public interface JsonLd
{
    String HIDDEN_VAR_PREFIX = "http://json-rql.org/var#";

    /**
     * Obtains a static RDF graph from the given value, in a form suitable for conversion to JSON-LD
     * using the JSON-LD Java library.
     * <p>
     * Note that this will be lossy, as <b>json-rql</b> features such as in-line filters will be lost.
     * Use another {@link Jrql.Visitor} to visit all features of the value.
     */
    static Object asGraph(Value value)
    {
        return asGraph(value, false);
    }

    /**
     * Obtains a static RDF graph from the given value, in a form suitable for conversion to JSON-LD
     * using the JSON-LD Java library.
     * <p>
     * If <code>strict</code> is <code>true</code>, <b>json-rql</b> features such as in-line filters
     * and variables will throw an {@link IllegalArgumentException}. Otherwise, these will be lost or hidden.
     */
    static Object asGraph(Value value, boolean strict) throws IllegalArgumentException
    {
        return Jrql.map(value, new Jrql.Transform<Object>()
        {
            @Override
            public Object map(Variable variable)
            {
                if (strict)
                    throw new IllegalArgumentException("Variable found in conversion to JSON-LD");

                // We hide the variable name in a json-rql.org URI
                return singletonMap("@id", asIRI(variable));
            }

            @Override
            public Object map(Subject po)
            {
                return JsonLd.asGraph(po, strict);
            }

            @Override
            public Object map(Name name)
            {
                return singletonMap("@id", name.toString());
            }

            @Override
            public Object map(Literal literal)
            {
                if (literal.language().isPresent() || literal.type().isPresent())
                    return new ObjectMapper().convertValue(literal, Map.class);
                else
                    return literal.value();
            }

            @Override
            public Object map(InlineFilter inlineFilter)
            {
                if (strict)
                    throw new IllegalArgumentException("In-line filter found in conversion to JSON-LD");

                return JsonLd.asGraph(inlineFilter.variable());
            }
        });
    }

    static Map asGraph(Subject po)
    {
        return asGraph(po, false);
    }

    static Map asGraph(Subject po, boolean strict) throws IllegalArgumentException
    {
        final Map<String, Object> graph = new HashMap<>();
        po.id().ifPresent(id -> graph.put("@id", asIRI(id)));
        po.type().ifPresent(type -> graph.put("@type", asIRI(type)));
        po.properties().forEach((identifier, values) -> graph
            .put(asIRI(identifier), values.stream().map(value -> JsonLd.asGraph(value, strict)).collect(toList())));
        return graph;
    }

    static Map asGraph(Group group)
    {
        return asGraph(group, false);
    }

    static Map asGraph(Group group, boolean strict) throws IllegalArgumentException
    {
        if (strict && group.filter().isPresent())
            throw new IllegalArgumentException("Filter found in conversion to JSON-LD");

        return group.graph().map(
            graph -> singletonMap("@graph", graph.stream()
                .map(subject -> JsonLd.asGraph(subject, strict)).collect(toList())))
            .orElse(emptyMap());
    }

    static String asIRI(Id id)
    {
        return Jrql.map(id, new Jrql.Transform<String>()
        {
            @Override
            public String map(Variable variable)
            {
                return HIDDEN_VAR_PREFIX + variable.name();
            }

            @Override
            public String map(Name name)
            {
                return name.toString();
            }
        });
    }

    static boolean isHiddenVar(String value)
    {
        return value.startsWith(HIDDEN_VAR_PREFIX);
    }

    static String unhide(String value)
    {
        return isHiddenVar(value) ? value.substring(HIDDEN_VAR_PREFIX.length()) : value;
    }

    static Map<String, Object> asJsonLd(Context context)
    {
        final HashMap<String, Object> asMap = new HashMap<>();
        context.base().ifPresent(base -> asMap.put("@base", base.toString()));
        context.vocab().ifPresent(vocab -> asMap.put("@vocab", vocab.toString()));
        context.language().ifPresent(lang -> asMap.put("@language", lang));
        context.names().forEach((name, termDef) -> asMap.put(name.toString(), asJsonLd(termDef)));
        return asMap;
    }

    static Object asJsonLd(Context.TermDef termDef)
    {
        final HashMap<String, Object> asMap = new HashMap<>();
        termDef.id().ifPresent(id -> asMap.put("@id", id.toString()));
        termDef.container().ifPresent(container -> asMap.put("@container", container.tag()));
        termDef.reverse().ifPresent(reverse -> asMap.put("@reverse", asJsonLd(reverse)));
        termDef.type().ifPresent(type -> asMap.put("@type", type.toString()));
        termDef.language().ifPresent(lang -> asMap.put("@language", lang));
        return asMap.keySet().equals(singleton("@id")) ? asMap.get("@id") : asMap;
    }
}
