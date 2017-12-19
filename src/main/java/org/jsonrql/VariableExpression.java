package org.jsonrql;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import java.io.IOException;

@SuppressWarnings("WeakerAccess")
@JsonDeserialize(using = VariableExpression.Deserializer.class)
@JsonSerialize(using = VariableExpression.Serializer.class)
public class VariableExpression
{
    private final String variable;

    public VariableExpression(String variable)
    {
        this.variable = variable;
    }

    public String variable()
    {
        return variable;
    }

    public static class Deserializer extends JsonDeserializer<VariableExpression>
    {
        @Override
        public VariableExpression deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
        {
            if (p.getCurrentToken() == JsonToken.VALUE_STRING)
                return new VariableExpression(p.getText());
            else
                throw new InvalidFormatException(p, "String expected", p.getCurrentValue(), String.class);
        }
    }

    public static class Serializer extends JsonSerializer<VariableExpression>
    {
        @Override
        public void serialize(VariableExpression value, JsonGenerator gen,
                              SerializerProvider serializers) throws IOException
        {
            gen.writeString(value.variable());
        }
    }
}
