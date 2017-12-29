package org.jsonrql.jena;

import org.apache.jena.query.QueryFactory;
import org.jsonrql.Subject;
import org.junit.jupiter.api.Test;

import static org.jsonrql.Id.id;
import static org.jsonrql.InlineFilter.filter;
import static org.jsonrql.Literal.literal;
import static org.jsonrql.Query.*;
import static org.jsonrql.Result.STAR;
import static org.jsonrql.Subject.subject;
import static org.jsonrql.jena.JsonRqlJena.toSparql;
import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonRqlJenaTest
{
    @Test
    void testSelectAll()
    {
        assertEquals(QueryFactory.create("SELECT * WHERE { ?s  ?p  ?o}"),
                     toSparql(select(STAR).where(subject("?s").with("?p", "?o"))));
    }

    @Test
    void testConstruct()
    {
        final Subject subject = subject("?s").with("?p", "?o");
        assertEquals(QueryFactory.create("CONSTRUCT { ?s  ?p  ?o} WHERE { ?s  ?p  ?o}"),
                     toSparql(construct(subject).where(subject)));
    }

    @Test
    void testExample()
    {
        assertEquals(
            QueryFactory.create(
                "SELECT  ?p ?c\n" +
                    "WHERE\n" +
                    "  { ?c  <http://xmlns.com/foaf/0.1/name>  \"York\"@en .\n" +
                    "    ?p  <http://dbpedia.org/ontology/birthPlace>  ?c ;\n" +
                    "        a                     <http://dbpedia.org/ontology/Artist>\n" +
                    "  }"),
        toSparql(
            select("?p", "?c")
                .where(
                    subject("?p")
                        .type("dbpedia-owl:Artist")
                        .with("dbpedia-owl:birthPlace", subject("?c")
                            .with("http://xmlns.com/foaf/0.1/name", literal("York").language("en"))))
            .prefix("dbpedia-owl", "http://dbpedia.org/ontology/")));
    }

    @Test
    void testArtistsGhent()
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
            toSparql(
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
                    .prefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#")
                    .prefix("dbpedia", "http://dbpedia.org/resource/")
                    .prefix("dbpedia-owl", "http://dbpedia.org/ontology/")));
    }

    @Test
    void testBsbm1()
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
            toSparql(
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
