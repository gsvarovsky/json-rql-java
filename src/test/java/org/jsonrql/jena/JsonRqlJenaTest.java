/*
 * Copyright (c) George Svarovsky 2019. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package org.jsonrql.jena;

import org.apache.jena.query.QueryFactory;
import org.apache.jena.update.UpdateFactory;
import org.jsonrql.Subject;
import org.junit.jupiter.api.Test;

import static org.jsonrql.Context.context;
import static org.jsonrql.Id.id;
import static org.jsonrql.InlineFilter.filter;
import static org.jsonrql.Literal.literal;
import static org.jsonrql.Query.*;
import static org.jsonrql.Result.STAR;
import static org.jsonrql.Subject.subject;
import static org.jsonrql.Variable.var;
import static org.jsonrql.jena.JsonRqlJena.asSparqlQuery;
import static org.jsonrql.jena.JsonRqlJena.asSparqlUpdate;
import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonRqlJenaTest
{
    @Test void testSelectAll()
    {
        assertEquals(QueryFactory.create("SELECT * WHERE { ?s  ?p  ?o}"),
                     asSparqlQuery(select(STAR).where(subject("?s").with("?p", "?o"))));
    }

    @Test void testConstruct()
    {
        final Subject subject = subject("?s").with("?p", "?o");
        assertEquals(QueryFactory.create("CONSTRUCT { ?s  ?p  ?o} WHERE { ?s  ?p  ?o}"),
                     asSparqlQuery(construct(subject).where(subject)));
    }

    @Test void testDescribeName()
    {
        assertEquals(QueryFactory.create("DESCRIBE <meld:fred>"), asSparqlQuery(describe("meld:fred")));
    }

    @Test void testDeleteNameOnly()
    {
        assertEquals(UpdateFactory.create("DELETE WHERE {}").toString(),
                     asSparqlUpdate(delete(subject("meld:fred"))).toString());
    }

    @Test void testDeleteSubject()
    {
        assertEquals(UpdateFactory.create("DELETE WHERE {<meld:fred> ?p ?o}").toString(),
                     asSparqlUpdate(delete(subject("meld:fred").with(var("p"), var("o")))).toString());
    }

    @Test void testExample()
    {
        assertEquals(
            QueryFactory.create(
                "SELECT  ?p ?c\n" +
                    "WHERE\n" +
                    "  { ?c  <http://xmlns.com/foaf/0.1/name>  \"York\"@en .\n" +
                    "    ?p  <http://dbpedia.org/ontology/birthPlace>  ?c ;\n" +
                    "        a                     <http://dbpedia.org/ontology/Artist>\n" +
                    "  }"),
            asSparqlQuery(
                select("?p", "?c")
                    .where(
                        subject("?p")
                            .type("dbpedia-owl:Artist")
                            .with("dbpedia-owl:birthPlace", subject("?c")
                                .with("http://xmlns.com/foaf/0.1/name", literal("York").language("en"))))
                    .context(context().prefix("dbpedia-owl", "http://dbpedia.org/ontology/"))));
    }

    @Test void testArtistsGhent()
    {
        assertEquals(
            QueryFactory.create(
                "CONSTRUCT \n" +
                    "  { \n" +
                    "    ?person <http://www.w3.org/2000/01/rdf-schema#label> ?name .\n" +
                    "    ?person <http://dbpedia.org/ontology/birthPlace> ?city .\n" +
                    "    ?person <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/Artist> .\n" +
                    "  }\n" +
                    "WHERE\n" +
                    "  { ?person   <http://www.w3.org/2000/01/rdf-schema#label>  ?name ;\n" +
                    "              <http://dbpedia.org/ontology/birthPlace>  ?city ;\n" +
                    "              a                     <http://dbpedia.org/ontology/Artist> .\n" +
                    "    ?city     <http://www.w3.org/2000/01/rdf-schema#label>  \"Ghent\"@en ;\n" +
                    "              <http://www.w3.org/2000/01/rdf-schema#label>  ?cityName ;\n" +
                    "              <http://dbpedia.org/ontology/country>  ?country .\n" +
                    "    ?country  <http://www.w3.org/2000/01/rdf-schema#label>  \"Belgium\"@en\n" +
                    "  }\n"),
            asSparqlQuery(
                construct(
                    subject("?person")
                        .type("dbpedia-owl:Artist")
                        .with("dbpedia-owl:birthPlace", "?city")
                        .with("rdfs:label", "?name"))
                    .where(
                        subject("?person")
                            .type("dbpedia-owl:Artist")
                            .with("dbpedia-owl:birthPlace", subject("?city")
                                .with("dbpedia-owl:country", subject("?country")
                                    .with("rdfs:label", literal("Belgium").language("en")))
                                .with("rdfs:label", id("?cityName"), literal("Ghent").language("en")))
                            .with("rdfs:label", "?name"))
                    .context(context().prefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#")
                                 .prefix("dbpedia", "http://dbpedia.org/resource/")
                                 .prefix("dbpedia-owl", "http://dbpedia.org/ontology/"))));
    }

    @Test void testBsbm1()
    {
        assertEquals(
            QueryFactory.create(
                "SELECT DISTINCT  ?product ?label\n" +
                    "WHERE\n" +
                    "  { ?product  <rdfs:label>          ?label ;\n" +
                    "              <bsbm:productPropertyNumeric1>  ?value1 ;\n" +
                    "              <bsbm:productFeature>  <bsbm-inst:ProductFeature814> ;\n" +
                    "              <bsbm:productFeature>  <bsbm-inst:ProductFeature815> ;\n" +
                    "              a                     <bsbm-inst:ProductType105>\n" +
                    "    FILTER ( ?value1 > 486 )\n" +
                    "  }\n" +
                    "ORDER BY ?label\n" +
                    "LIMIT   10"),
            asSparqlQuery(
                distinct("?product", "?label")
                    .where(
                        subject("?product")
                            .type("bsbm-inst:ProductType105")
                            .with("rdfs:label", "?label")
                            .with("bsbm:productFeature",
                                  subject("bsbm-inst:ProductFeature815"),
                                  subject("bsbm-inst:ProductFeature814"))
                            .with("bsbm:productPropertyNumeric1",
                                  filter("?value1", "@gt", literal(486))))
                    .orderBy("?label")
                    .limit(10)));
    }
}
