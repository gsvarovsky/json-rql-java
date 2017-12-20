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
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.jsonrql.Jrql;
import org.jsonrql.Result.Star;
import org.jsonrql.Variable;
import org.jsonrql.VariableAssignment;

import java.io.IOException;

import static org.apache.jena.riot.Lang.JSONLD;
import static org.jsonrql.Variable.hideVars;
import static org.jsonrql.Variable.isHiddenVar;

public interface JenaQueryFactory
{
    static Query toSparql(org.jsonrql.Query jrqlQuery) throws IOException
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
        final String jsonLd = JsonUtils.toString(hideVars(jrqlQuery.where()));
        query.setQueryPattern(new ElementTriplesBlock(toPattern(toModel(jsonLd))));
        return query;
    }

    static Model toModel(String jsonLd)
    {
        final Model model = ModelFactory.createDefaultModel();
        RDFParser.fromString(jsonLd).lang(JSONLD).build().parse(model.getGraph());
        return model;
    }

    static BasicPattern toPattern(Model model)
    {
        final BasicPattern pattern = new BasicPattern();
        model.listStatements().forEachRemaining(
            statement -> pattern.add(new Triple(unhideVar(statement.getSubject().asNode()),
                                                unhideVar(statement.getPredicate().asNode()),
                                                unhideVar(statement.getObject().asNode()))));
        return pattern;
    }

    static Node unhideVar(Node node)
    {
        return node.isURI() && isHiddenVar(node.getURI()) ? new Node_Variable(Variable.unhide(node.getURI())) : node;
    }
}
