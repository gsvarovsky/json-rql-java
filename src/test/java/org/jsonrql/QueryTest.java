/*
 * Copyright (c) George Svarovsky 2019. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package org.jsonrql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.URL;
import java.util.Set;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("SameParameterValue")
class QueryTest
{
    private static ObjectMapper objectMapper = new ObjectMapper();

    @ParameterizedTest
    @MethodSource("testCases")
    void testAll(URL testCase) throws IOException
    {
        final Query javaJsonRql = objectMapper.readValue(testCase, Query.class);
        final String genJsonRql = objectMapper.writeValueAsString(javaJsonRql);

        assertEquals(objectMapper.readValue(testCase, ObjectNode.class),
                     objectMapper.readValue(genJsonRql, ObjectNode.class));
    }

    private static Stream<URL> testCases() throws IOException
    {
        @SuppressWarnings("unchecked") final Set<String> testNames =
            objectMapper.readValue(QueryTest.class.getResource("testcases.json"), Set.class);
        return testNames.stream().map(
            tc -> QueryTest.class.getResource(format("/json-rql-%s/test/data/%s.json",
                                                     System.getProperty("json-rql.version"),
                                                     tc)));
    }
}
