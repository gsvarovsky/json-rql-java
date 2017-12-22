package org.jsonrql;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Map;
import java.util.Optional;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

public abstract class Literal implements Value
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

    public abstract Optional<String> type();

    public Literal type(String type)
    {
        return new QualifiedLiteral(value, type, language().orElse(null));
    }

    private Literal(Object value)
    {
        this.value = value;
    }

    public Object value()
    {
        return value;
    }

    @Override
    public void accept(Visitor visitor)
    {
        visitor.visit(this);
    }

    @JsonDeserialize
    static final class PlainLiteral extends Literal
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
        public Optional<String> type()
        {
            return Optional.empty();
        }

        @Override
        public void accept(Visitor visitor)
        {
            visitor.visit(this);
        }

        @Override
        @JsonValue
        public Object asJsonLd()
        {
            return value();
        }
    }

    @JsonDeserialize
    static final class QualifiedLiteral extends Literal
    {
        private final String type;
        private final String language;

        @JsonCreator
        QualifiedLiteral(@JsonProperty(value = "@value", required = true) Object value,
                         @JsonProperty("@type") String type,
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
        public Optional<String> type()
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
        public Object asJsonLd()
        {
            return new ObjectMapper().convertValue(this, Map.class);
        }

        @JsonProperty("@type")
        @JsonInclude(NON_NULL)
        @SuppressWarnings("unused")
        private String getType()
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
