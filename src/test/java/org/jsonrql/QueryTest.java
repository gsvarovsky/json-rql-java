package org.jsonrql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

import java.io.IOException;
import java.net.URL;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        final Set testNames = objectMapper.readValue(QueryTest.class.getResource("testcases.json"), Set.class);
        final Pattern matchTestCase = Pattern.compile("([^/]+)\\.json$");
        return new Reflections("org.jsonrql.package.test.data", new ResourcesScanner())
            .getResources(matchTestCase).stream().sorted().filter(tc -> {
                final Matcher match = matchTestCase.matcher(tc);
                assertTrue(match.find());
                final String testName = match.group(1);
                final boolean isTestCase = testNames.contains(testName);
                if (!isTestCase && !"todo".equals(testName)) // TODO: Move this file in the json-rql project
                    System.out.println("Not testing example " + testName);
                return isTestCase;
            })
            .map(tc -> QueryTest.class.getResource("/" + tc));
    }
}
