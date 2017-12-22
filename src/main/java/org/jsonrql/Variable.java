package org.jsonrql;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Collections.singletonMap;

@JsonDeserialize
public final class Variable implements Id
{
    private static String HIDDEN_VAR_PREFIX = "http://json-rql.org/var#";
    private static Pattern VAR_PATTERN = Pattern.compile("\\?([\\d\\w]+)");

    private final String name;

    @JsonCreator
    private Variable(String value)
    {
        if (value == null)
            throw new NullPointerException("Variable name cannot be null");

        final Matcher match = VAR_PATTERN.matcher(value);
        if (!match.matches())
            throw new IllegalArgumentException("Not a variable");

        this.name = match.group(1);
    }

    @Override
    public void accept(Visitor visitor)
    {
        visitor.visit(this);
    }

    public String name()
    {
        return name;
    }

    @Override
    public Map asJsonLd()
    {
        return singletonMap("@id", asIRI());
    }

    @Override
    public String asIRI()
    {
        return HIDDEN_VAR_PREFIX + name;
    }

    @Override
    @JsonValue
    public String toString()
    {
        return "?" + name;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name);
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof Variable && equals((Variable)obj);
    }

    private boolean equals(Variable that)
    {
        return that != null && this.name.equals(that.name);
    }

    static Optional<Variable> matchVar(Object value)
    {
        return Optional.ofNullable(value).filter(String.class::isInstance).map(String.class::cast)
            .map(VAR_PATTERN::matcher).filter(Matcher::matches).map(match -> new Variable(match.group()));
    }

    public static boolean isHiddenVar(String value)
    {
        return value.startsWith(HIDDEN_VAR_PREFIX);
    }

    public static String unhide(String value)
    {
        return isHiddenVar(value) ? value.substring(HIDDEN_VAR_PREFIX.length()) : value;
    }
}
