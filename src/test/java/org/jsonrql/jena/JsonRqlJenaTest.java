package org.jsonrql.jena;

import com.google.common.collect.ImmutableMap;
import org.apache.jena.query.QueryFactory;
import org.junit.jupiter.api.Test;

import static org.jsonrql.Result.STAR;
import static org.jsonrql.jena.JsonRqlJena.toSparql;
import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonRqlJenaTest
{
    @Test
    void testAll()
    {
        assertEquals(QueryFactory.create("SELECT * WHERE { ?s  ?p  ?o}"),
                     toSparql(new org.jsonrql.Query()
                                  .select(STAR)
                                  .where(ImmutableMap.of("@id", "?s", "?p", "?o"))));
    }
}
