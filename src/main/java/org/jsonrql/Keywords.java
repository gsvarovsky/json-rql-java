/*
 * Copyright (c) George Svarovsky 2019. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package org.jsonrql;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;

public final class Keywords
{
    public static final Keywords KEYWORDS;

    static
    {
        try
        {
            final URL resource = Keywords.class.getClassLoader().getResource("keywords.json");
            KEYWORDS = new ObjectMapper().readValue(resource, Keywords.class);
        }
        catch (IOException e)
        {
            throw new Error(e);
        }
    }

    public final Map<String, Keyword> operators;
    public final Map<String, Keyword> clauses;
    public final Map<String, Keyword> groupPatterns;

    @JsonCreator
    private Keywords(@JsonProperty("operators") Map<String, Keyword> operators,
                     @JsonProperty("clauses") Map<String, Keyword> clauses,
                     @JsonProperty("groupPatterns") Map<String, Keyword> groupPatterns)
    {
        this.operators = unmodifiableMap(operators);
        this.clauses = unmodifiableMap(clauses);
        this.groupPatterns = unmodifiableMap(groupPatterns);
    }

    public static final class Keyword
    {
        public final String sparql;
        public final boolean associative;
        public final boolean aggregation;

        @JsonCreator
        private Keyword(@JsonProperty("sparql") String sparql,
                        @JsonProperty("associative") boolean associative,
                        @JsonProperty("aggregation") boolean aggregation)
        {
            this.sparql = sparql;
            this.associative = associative;
            this.aggregation = aggregation;
        }
    }
}
