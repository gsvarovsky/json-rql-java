/*
 * Copyright (c) George Svarovsky 2020. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package org.jsonrql.jena;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.syntax.Template;
import org.jsonrql.*;
import org.jsonrql.jsonld.JsonLd;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.jena.graph.NodeFactory.createURI;
import static org.apache.jena.graph.NodeFactory.createVariable;
import static org.jsonrql.jena.JsonRqlJena.asExpr;
import static org.jsonrql.jena.JsonRqlJena.asPattern;

public class JsonRqlJenaQueryBuilder extends JsonRqlJenaBuilder<Query>
{
    private final Query query = QueryFactory.create();

    private final Read<?> readQuery;

    JsonRqlJenaQueryBuilder(org.jsonrql.Read<?> readQuery)
    {
        super(readQuery);
        this.readQuery = readQuery;
    }

    @Override public Query build()
    {
        if (readQuery instanceof Select)
            this.addSelectResults(((Select)readQuery).select());
        else if (readQuery instanceof Distinct)
            addDistinctResults(((Distinct)readQuery).distinct());
        else if (readQuery instanceof Construct)
            addConstruct(((Construct)readQuery).construct());
        else if (readQuery instanceof Describe)
            addDescribe(((Describe)readQuery).describe());

        // A query must always have an element group at the top level
        whereElement().ifPresent(query::setQueryPattern);
        readQuery.orderBy().ifPresent(this::setOrderBy);
        readQuery.limit().ifPresent(query::setLimit);
        readQuery.offset().ifPresent(query::setOffset);
        return query;
    }

    private void addSelectResults(List<Result> select)
    {
        query.setQuerySelectType();
        select.forEach(result -> result.accept(new Jrql.Visitor()
        {
            @Override
            public void visit(Result.Star star)
            {
                query.setQueryResultStar(true);
            }

            @Override
            public void visit(Variable variable)
            {
                query.addResultVar(variable.name());
            }
        }));
    }

    private void addDistinctResults(List<Result> distinct)
    {
        addSelectResults(distinct);
        query.setDistinct(true);
    }

    private void addConstruct(List<Subject> construct)
    {
        query.setQueryConstructType();
        query.setConstructTemplate(new Template(asPattern(
            construct.stream().map(JsonLd::asGraph).collect(toList()), ctx)));
    }

    private void addDescribe(List<Id> describe)
    {
        query.setQueryDescribeType();
        describe.forEach(id -> query.addDescribeNode(requireNonNull(Jrql.map(id, new Jrql.Transform<Node>()
        {
            @Override public Node map(Variable variable)
            {
                return createVariable(variable.name());
            }

            @Override public Node map(Name name)
            {
                return createURI(jrql.context().resolve(name).toString());
            }
        }))));
    }

    public void setOrderBy(List<Expression> orderBy)
    {
        orderBy.forEach(e -> query.addOrderBy(SSE.parseExpr(asExpr(e), prefixes), -2/*TODO*/));
    }
}
