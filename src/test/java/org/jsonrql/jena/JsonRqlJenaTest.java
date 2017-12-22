package org.jsonrql.jena;

import org.apache.jena.query.QueryFactory;
import org.jsonrql.PatternObject;
import org.junit.jupiter.api.Test;

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
}
