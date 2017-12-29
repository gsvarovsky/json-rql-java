package org.jsonrql.jsonld;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsonrql.*;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonMap;
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
     *
     * @return this value as a static RDF graph
     */
    static Object asGraph(Value value)
    {
        return Jrql.map(value, new Jrql.Transform<Object>()
        {
            @Override
            public Object map(Variable variable)
            {
                // We hide the variable name in a json-rql.org URI
                //noinspection unchecked
                return singletonMap("@id", asIRI(variable));
            }

            @Override
            public Object map(Subject po)
            {
                //noinspection unchecked
                return JsonLd.asGraph(po);
            }

            @Override
            public Object map(Name name)
            {
                //noinspection unchecked
                return singletonMap("@id", name.name());
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
                //noinspection unchecked
                return JsonLd.asGraph(inlineFilter.variable());
            }
        });
    }

    static Map asGraph(Subject po)
    {
        final Map<String, Object> graph = new HashMap<>();
        po.id().ifPresent(id -> graph.put("@id", asIRI(id)));
        po.type().ifPresent(type -> graph.put("@type", asIRI(type)));
        po.properties().forEach((identifier, values) -> graph
            .put(asIRI(identifier), values.stream().map(JsonLd::asGraph).collect(toList())));
        return graph;
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
                return name.name();
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
}
