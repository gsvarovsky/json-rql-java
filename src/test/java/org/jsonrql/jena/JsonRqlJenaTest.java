package org.jsonrql.jena;

import org.apache.jena.query.QueryFactory;
import org.jsonrql.PatternObject;
import org.junit.jupiter.api.Test;

import static org.jsonrql.PatternObject.POBJ;
import static org.jsonrql.Query.JRQL;
import static org.jsonrql.Result.STAR;
import static org.jsonrql.jena.JsonRqlJena.toSparql;
import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonRqlJenaTest
{
    @Test
    void testSelectAll()
    {
        assertEquals(QueryFactory.create("SELECT * WHERE { ?s  ?p  ?o}"),
                     toSparql(JRQL.select(STAR).where(POBJ.id("?s").with("?p", "?o"))));
    }

    @Test
    void testConstruct()
    {
        final PatternObject patternObject = POBJ.id("?s").with("?p", "?o");
        assertEquals(QueryFactory.create("CONSTRUCT { ?s  ?p  ?o} WHERE { ?s  ?p  ?o}"),
                     toSparql(JRQL.construct(patternObject).where(patternObject)));
    }
}
