package org.jsonrql.jena;

import org.apache.jena.query.QueryFactory;
import org.jsonrql.PatternObject;
import org.junit.jupiter.api.Test;

import static org.jsonrql.Id.id;
import static org.jsonrql.Literal.literal;
import static org.jsonrql.PatternObject.subject;
import static org.jsonrql.Query.construct;
import static org.jsonrql.Query.select;
import static org.jsonrql.Result.STAR;
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
        final PatternObject patternObject = subject("?s").with("?p", "?o");
        assertEquals(QueryFactory.create("CONSTRUCT { ?s  ?p  ?o} WHERE { ?s  ?p  ?o}"),
                     toSparql(construct(patternObject).where(patternObject)));
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
            toSparql(construct(
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
}
