package org.jsonrql;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.fasterxml.jackson.core.JsonToken.VALUE_STRING;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.jsonrql.Jrql.badMapping;
import static org.jsonrql.Jrql.badToken;

@JsonDeserialize(using = Variable.Deserializer.class)
@JsonSerialize(using = ToStringSerializer.class)
public final class Variable implements Expression
{
    private static String HIDDEN_VAR_PREFIX = "http://json-rql.org/var#";
    private static Pattern VAR_PATTERN = Pattern.compile("\\?([\\d\\w]+)");

    private final String name;

    public Variable(String name)
    {
        if (name == null)
            throw new NullPointerException("Variable name cannot be null");

        this.name = name;
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

    private Object asHidden()
    {
        return HIDDEN_VAR_PREFIX + name;
    }

    @Override
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

    public boolean equals(Variable that)
    {
        return that != null && this.name.equals(that.name);
    }

    public static Optional<Variable> matchVar(Object value)
    {
        return Optional.ofNullable(value).filter(String.class::isInstance).map(String.class::cast)
            .map(VAR_PATTERN::matcher).filter(Matcher::matches).map(match -> new Variable(match.group(1)));
    }

    private static Object hideVar(Object value)
    {
        return matchVar(value).map(Variable::asHidden).orElse(value);
    }

    public static Object hideVars(Object value)
    {
        if (value instanceof List)
        {
            return ((List<?>) value).stream().map(Variable::hideVars).collect(toList());
        }
        else if (value instanceof Map)
        {
            //noinspection unchecked
            return ((Map<String, ?>) value).entrySet().stream().collect(toMap(
                e -> hideVars(e.getKey()), e -> e.getValue() instanceof Map || !e.getKey().startsWith("@") ?
                    hideVars(e.getValue()) : hideVar(e.getValue())));
        }
        else if (matchVar(value).isPresent())
        {
            return singletonMap("@id", hideVar(value));
        }
        else
        {
            return value;
        }
    }

    public static boolean isHiddenVar(String value)
    {
        return value.startsWith(HIDDEN_VAR_PREFIX);
    }

    public static String unhide(String value)
    {
        return isHiddenVar(value) ? value.substring(HIDDEN_VAR_PREFIX.length()) : value;
    }

    public static class Deserializer extends JsonDeserializer<Variable>
    {
        @Override
        public Variable deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
        {
            switch (p.getCurrentToken())
            {
                case VALUE_STRING:
                    return matchVar(p.getText()).orElseThrow(() -> badMapping(p, "Not a variable"));

                default:
                    throw badToken(p, VALUE_STRING);
            }
        }
    }
}
