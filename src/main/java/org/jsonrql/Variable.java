package org.jsonrql;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

@JsonDeserialize
public final class Variable implements Id, Result
{
    private static Pattern VAR_PATTERN = Pattern.compile("\\?([\\d\\w]+)");

    private final String name;

    public static Variable generate()
    {
        return new Variable("?" + randomAlphanumeric(4));
    }

    @JsonCreator
    private Variable(String id)
    {
        if (id == null)
            throw new NullPointerException("Variable name cannot be null");

        final Matcher match = VAR_PATTERN.matcher(id);
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
    public boolean equals(Object o)
    {
        return this == o || o instanceof Variable && this.name.equals(((Variable) o).name);
    }

    static Optional<Variable> matchVar(Object value)
    {
        return Optional.ofNullable(value).filter(String.class::isInstance).map(String.class::cast)
            .map(VAR_PATTERN::matcher).filter(Matcher::matches).map(match -> new Variable(match.group()));
    }
}
