package org.jsonrql;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;

public final class Keywords
{
    public static final Keywords KEYWORDS;

    static
    {
        try
        {
            KEYWORDS = new ObjectMapper().readValue(Keywords.class.getResource("keywords.json"), Keywords.class);
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
