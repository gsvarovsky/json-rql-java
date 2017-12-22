package org.jsonrql.jena;

import com.github.jsonldjava.utils.JsonUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_Variable;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.Template;
import org.jsonrql.Jrql;
import org.jsonrql.PatternObject;
import org.jsonrql.Result.Star;
import org.jsonrql.Variable;
import org.jsonrql.VariableAssignment;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.apache.jena.riot.Lang.JSONLD;
import static org.jsonrql.Variable.isHiddenVar;

public interface JsonRqlJena
{
    static Query toSparql(org.jsonrql.Query jrqlQuery)
    {
        final Query query = new Query();
        jrqlQuery.select().ifPresent(select -> {
            query.setQuerySelectType();
            select.forEach(result -> result.accept(new Jrql.Visitor()
            {
                @Override
                public void visit(Star star)
                {
                    query.setQueryResultStar(true);
                }

                @Override
                public void visit(VariableAssignment variableAssignment)
                {
                    query.addResultVar(variableAssignment.variable().name());
                }
            }));
        });
        jrqlQuery.construct().ifPresent(construct -> {
            query.setQueryConstructType();
            query.setConstructTemplate(new Template(JsonRqlJena.toPattern(
                construct.stream().map(PatternObject::asJsonLd).collect(toList()),
                jrqlQuery.context())));
        });
        final ElementGroup group = new ElementGroup(); // Jena always has a group at top level
        jrqlQuery.where().forEach(pattern -> pattern.accept(new Jrql.Visitor()
        {
            @Override
            public void visit(PatternObject patternObject)
            {
                group.addElement(new ElementPathBlock(
                    JsonRqlJena.toPattern(patternObject.asJsonLd(), jrqlQuery.context())));
            }
        }));
        query.setQueryPattern(group);
        return query;
    }

    static BasicPattern toPattern(List graph, Map context)
    {
        Map jsonld = new HashMap();
        //noinspection unchecked
        jsonld.put("@graph", graph);
        return toPattern(jsonld, context);
    }

    static BasicPattern toPattern(Map graph, Map context)
    {
        //noinspection unchecked
        graph.put("@context", context);
        return toPattern(graph);
    }

    static BasicPattern toPattern(Object jsonld)
    {
        try
        {
            final Model model = ModelFactory.createDefaultModel();
            RDFParser.fromString(JsonUtils.toString(jsonld)).lang(JSONLD).build().parse(model.getGraph());
            final BasicPattern pattern = new BasicPattern();
            model.listStatements().forEachRemaining(
                statement -> pattern.add(new Triple(unhideVar(statement.getSubject().asNode()),
                                                    unhideVar(statement.getPredicate().asNode()),
                                                    unhideVar(statement.getObject().asNode()))));
            return pattern;
        }
        catch (IOException e)
        {
            throw new AssertionError(); // Must be something seriously wrong with json-rql itself
        }
    }

    static Node unhideVar(Node node)
    {
        return node.isURI() && isHiddenVar(node.getURI()) ? new Node_Variable(Variable.unhide(node.getURI())) : node;
    }
}
