package org.jsonrql;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.jsonrql.Result.Star;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public interface Jrql
{
    interface Visitor
    {
        default void visit(Star star) {}
        default void visit(Query query) {}
        default void visit(VariableAssignment variableAssignment) {}
        default void visit(Variable variable) {}
    }

    void accept(Visitor visitor);

    static JsonMappingException badToken(JsonParser p, JsonToken... allowedTokens)
    {
        return badMapping(p, "Expected one of %s", asList(allowedTokens));
    }

    static JsonMappingException badMapping(JsonParser p, String msg, Object... args)
    {
        return new JsonMappingException(p, format(msg, args));
    }
}
