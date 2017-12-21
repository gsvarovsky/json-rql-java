package org.jsonrql.jena;

import org.apache.jena.query.QueryFactory;
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
}
