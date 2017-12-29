package org.jsonrql;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;
import java.util.Optional;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.core.JsonToken.*;

@JsonDeserialize(using = Literal.Deserializer.class)
public abstract class Literal implements Value, Expression
{
    private final Object value;

    public static Literal literal(String value)
    {
        return new PlainLiteral(value);
    }

    public static Literal literal(int value)
    {
        return new PlainLiteral(value);
    }

    public static Literal literal(double value)
    {
        return new PlainLiteral(value);
    }

    public static Literal literal(boolean value)
    {
        return new PlainLiteral(value);
    }

    public abstract Optional<String> language();

    public Literal language(String language)
    {
        return new QualifiedLiteral(value, type().orElse(null), language);
    }

    public abstract Optional<Id> type();

    public Literal type(Id type)
    {
        return new QualifiedLiteral(value, type, language().orElse(null));
    }

    private Literal(Object value)
    {
        if (!(value instanceof String
            || value instanceof Integer
            || value instanceof Double
            || value instanceof Float
            || value instanceof Boolean))
            throw new IllegalArgumentException("Expected JSON atomic value type");

        this.value = value;
    }

    public Object value()
    {
        return value;
    }

    public static class Deserializer extends Jrql.Deserializer<Literal>
    {
        @Override
        public Literal deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
        {
            switch (p.getCurrentToken())
            {
                case START_OBJECT:
                    return ctxt.readValue(p, QualifiedLiteral.class);

                case VALUE_STRING:
                case VALUE_NUMBER_INT:
                case VALUE_NUMBER_FLOAT:
                case VALUE_TRUE:
                case VALUE_FALSE:
                    return ctxt.readValue(p, PlainLiteral.class);

                case VALUE_NULL:
                    return null;

                default:
                    throw badToken(p, START_OBJECT, VALUE_STRING, VALUE_NUMBER_INT, VALUE_NUMBER_FLOAT, VALUE_TRUE,
                                   VALUE_FALSE, VALUE_NULL);
            }
        }
    }

    @JsonDeserialize
    public static final class PlainLiteral extends Literal
    {
        @JsonCreator
        PlainLiteral(Object value)
        {
            super(value);
        }

        @Override
        @JsonIgnore
        public Optional<String> language()
        {
            return Optional.empty();
        }

        @Override
        @JsonIgnore
        public Optional<Id> type()
        {
            return Optional.empty();
        }

        @Override
        @JsonValue
        public Object value()
        {
            return super.value();
        }

        @Override
        public void accept(Visitor visitor)
        {
            visitor.visit(this);
        }
    }

    @JsonDeserialize
    public static final class QualifiedLiteral extends Literal
    {
        private final Id type;
        private final String language;

        @JsonCreator
        QualifiedLiteral(@JsonProperty(value = "@value", required = true) Object value,
                         @JsonProperty("@type") Id type,
                         @JsonProperty("@language") String language)
        {
            super(value);
            this.type = type;
            this.language = language;
        }

        @Override
        @JsonIgnore
        public Optional<String> language()
        {
            return Optional.ofNullable(language);
        }

        @Override
        @JsonIgnore
        public Optional<Id> type()
        {
            return Optional.ofNullable(type);
        }

        @Override
        @JsonProperty("@value")
        public Object value()
        {
            return super.value();
        }

        @Override
        public void accept(Visitor visitor)
        {
            visitor.visit(this);
        }

        @JsonProperty("@type")
        @JsonInclude(NON_NULL)
        @SuppressWarnings("unused")
        private Id getType()
        {
            return type;
        }

        @JsonProperty("@language")
        @JsonInclude(NON_NULL)
        @SuppressWarnings("unused")
        private String getLanguage()
        {
            return language;
        }
    }
}
