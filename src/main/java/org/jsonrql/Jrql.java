package org.jsonrql;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.jsonrql.Result.Star;

import static java.lang.String.format;
import static java.util.Arrays.asList;

public interface Jrql
{
    interface Visitor
    {
        default void visit(Star star) {}
        default void visit(Query query) {}
        default void visit(VariableAssignment variableAssignment) {}
        default void visit(Variable variable) {}
        default void visit(PatternObject patternObject) {}
        default void visit(Name name) {}
        default void visit(Text text) {}
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
