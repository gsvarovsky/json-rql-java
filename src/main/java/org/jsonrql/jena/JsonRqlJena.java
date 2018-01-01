package org.jsonrql.jena;

import com.github.jsonldjava.utils.JsonUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_Variable;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.Template;
import org.jsonrql.*;
import org.jsonrql.Result.Star;
import org.jsonrql.jsonld.JsonLd;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apache.jena.riot.Lang.JSONLD;
import static org.jsonrql.Keywords.KEYWORDS;
import static org.jsonrql.jsonld.JsonLd.*;

public interface JsonRqlJena
{
    static Query toSparql(org.jsonrql.Query jrqlQuery)
    {
        final Query query = new Query();
        final PrefixMapping prefixes = PrefixMapping.Factory.create().setNsPrefixes(jrqlQuery.prefixes());

        jrqlQuery.select().ifPresent(select -> addSelectResults(query, select));
        jrqlQuery.distinct().ifPresent(distinct -> {
            addSelectResults(query, distinct);
            query.setDistinct(true);
        });
        jrqlQuery.construct().ifPresent(construct -> {
            query.setQueryConstructType();
            query.setConstructTemplate(new Template(JsonRqlJena.asPattern(
                construct.stream().map(JsonLd::asGraph).collect(toList()),
                jrqlQuery.context())));
        });
        final ElementGroup group = new ElementGroup(); // Jena always has a group at top level
        jrqlQuery.where().forEach(pattern -> pattern.accept(new Jrql.Visitor()
        {
            @Override
            public void visit(Subject subject)
            {
                group.addElement(new ElementPathBlock(
                    JsonRqlJena.asPattern(asGraph(subject), jrqlQuery.context())));

                // Pull out any in-line filters recursively
                subject.values().forEach(this::extractFilters);
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
                            .forEach(expr -> group.addElement(new ElementFilter(SSE.parseExpr(expr, prefixes))));
                    }
                });
            }
        }));
        query.setQueryPattern(group);
        jrqlQuery.orderBy().ifPresent(orderBy -> orderBy.forEach(
            e -> query.addOrderBy(SSE.parseExpr(asExpr(e), prefixes), -2/*TODO*/)));
        jrqlQuery.limit().ifPresent(query::setLimit);
        jrqlQuery.offset().ifPresent(query::setOffset);
        return query;
    }

    static void addSelectResults(Query query, List<Result> select)
    {
        query.setQuerySelectType();
        select.forEach(result -> result.accept(new Jrql.Visitor()
        {
            @Override
            public void visit(Star star)
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
        return node.isURI() && isHiddenVar(node.getURI()) ? new Node_Variable(unhide(node.getURI())) : node;
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
