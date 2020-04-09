/*
 * Copyright (c) George Svarovsky 2020. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package org.jsonrql.jena;

import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.syntax.*;
import org.jsonrql.*;

import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static org.jsonrql.Keywords.KEYWORDS;
import static org.jsonrql.jena.JsonRqlJena.asPattern;
import static org.jsonrql.jsonld.JsonLd.asGraph;
import static org.jsonrql.jsonld.JsonLd.asJsonLd;

public abstract class JsonRqlJenaBuilder<T>
{
    protected final Query<?> jrql;
    protected final PrefixMapping prefixes;
    protected final Map<String, Object> ctx;

    JsonRqlJenaBuilder(Query<?> jrql)
    {
        this.jrql = jrql;
        this.ctx = asJsonLd(jrql.context());
        this.prefixes = PrefixMapping.Factory.create().setNsPrefixes(jrql.context().prefixes());
    }

    abstract T build();

    Optional<Element> whereElement()
    {
        final ElementGroup group = new ElementGroup();
        jrql.where().forEach(pattern -> addWhereTo(pattern, group));
        return group.isEmpty() ? Optional.empty() : Optional.of(group);
    }

    private void addWhereTo(Pattern pattern, ElementGroup elementGroup)
    {
        pattern.accept(new Jrql.Visitor()
        {
            @Override public void visit(Subject subject)
            {
                elementGroup.addElement(new ElementPathBlock(asPattern(asGraph(subject), ctx)));

                // Pull out any in-line filters recursively
                subject.values().forEach(this::extractFilters);
            }

            @Override public void visit(Group group)
            {
                group.graph().ifPresent(graph -> graph.forEach(this::visit));
                group.union().ifPresent(union -> {
                    final ElementUnion elementUnion = new ElementUnion();
                    union.forEach(pattern -> {
                        final ElementGroup subGroup = new ElementGroup();
                        addWhereTo(pattern, subGroup);
                        if (!subGroup.isEmpty())
                            elementUnion.addElement(subGroup);
                    });
                    elementGroup.addElement(elementUnion);
                });
            }

            // Mysteriously, this method cannot be in-lined due to an IllegalAccessError in the hotspot compiler
            // http://hg.openjdk.java.net/jdk8u/jdk8u/hotspot/rev/0b85ccd62409#l11.80
            void extractFilters(Value value)
            {
                value.accept(new Jrql.Visitor()
                {
                    @Override
                    public void visit(Subject subject)
                    {
                        subject.values().forEach(value -> value.accept(this));
                    }

                    @Override
                    public void visit(InlineFilter inlineFilter)
                    {
                        inlineFilter.filters().entrySet().stream()
                            .map(opRhs -> format("(%s %s %s)",
                                                 KEYWORDS.operators.get(opRhs.getKey()).sparql,
                                                 inlineFilter.variable(),
                                                 // TODO: This will probably break with @in
                                                 opRhs.getValue().stream()
                                                     .map(JsonRqlJena::asExpr).collect(joining(" "))))
                            .forEach(expr -> elementGroup.addElement(new ElementFilter(SSE.parseExpr(expr, prefixes))));
                    }
                });
            }
        });
    }
}
