/*
 * Copyright (c) George Svarovsky 2019. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package org.jsonrql.jena;

import com.github.jsonldjava.utils.JsonUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.update.UpdateRequest;
import org.jsonrql.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static org.apache.jena.riot.Lang.JSONLD;
import static org.jsonrql.jsonld.JsonLd.isHiddenVar;
import static org.jsonrql.jsonld.JsonLd.unhide;

public interface JsonRqlJena
{
    static Query asSparqlQuery(org.jsonrql.Query jrql)
    {
        return new JsonRqlJenaQueryBuilder(jrql).build();
    }

    static UpdateRequest asSparqlUpdate(org.jsonrql.Query jrql)
    {
        return new JsonRqlJenaUpdateBuilder(jrql).build();
    }

    static BasicPattern asPattern(List graph, Map context)
    {
        Map jsonld = new HashMap();
        //noinspection unchecked
        jsonld.put("@graph", graph);
        return asPattern(jsonld, context);
    }

    static BasicPattern asPattern(Map graph, Map context)
    {
        //noinspection unchecked
        graph.put("@context", context);
        return asPattern(graph);
    }

    static BasicPattern asPattern(Object jsonld)
    {
        final Model model = ModelFactory.createDefaultModel();
        RDFParser.fromString(toJson(jsonld)).lang(JSONLD).build().parse(model.getGraph());
        final BasicPattern pattern = new BasicPattern();
        model.listStatements().forEachRemaining(
            statement -> pattern.add(new Triple(unhideVar(statement.getSubject().asNode()),
                                                unhideVar(statement.getPredicate().asNode()),
                                                unhideVar(statement.getObject().asNode()))));
        return pattern;
    }

    static String toJson(Object jsonld)
    {
        try
        {
            return JsonUtils.toString(jsonld);
        }
        catch (IOException e)
        {
            throw new AssertionError(); // Must be something seriously wrong with json-rql itself
        }
    }

    static Node unhideVar(Node node)
    {
        return node.isURI() && isHiddenVar(node.getURI()) ? Var.alloc(unhide(node.getURI())) : node;
    }

    static String asExpr(Expression expression)
    {
        return Jrql.map(expression, new Jrql.Transform<String>()
        {
            @Override
            public String map(Variable variable)
            {
                return variable.toString();
            }

            @Override
            public String map(Name name)
            {
                // TODO: This will work for @vocab but not @base, see Query#prefixes
                return format(name.toString().contains(":") ? "%s" : "<%s>", name);
            }

            @Override
            public String map(Literal literal)
            {
                final String value = toJson(literal.value()); // TODO: not quite the same as SSE encoding
                return literal.type().isPresent() ? format("%s^^%s", value, JsonRqlJena.asExpr(literal.type().get()))
                    : literal.language().isPresent() ? format("%s@%s", value, literal.language().get())
                    : value;
            }
        });
    }
}
