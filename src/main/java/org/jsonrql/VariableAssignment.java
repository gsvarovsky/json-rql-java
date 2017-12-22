package org.jsonrql;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;

import static com.fasterxml.jackson.core.JsonToken.VALUE_STRING;

@JsonDeserialize(using = VariableAssignment.Deserializer.class)
@JsonSerialize(using = VariableAssignment.Serializer.class)
public final class VariableAssignment implements Result
{
    private final Variable variable;
    private final Value value;

    public VariableAssignment(Variable variable, Value value)
    {
        this.variable = variable;
        this.value = value;
    }

    @Override
    public void accept(Visitor visitor)
    {
        visitor.visit(this);
    }

    public Variable variable()
    {
        return variable;
    }

    public Value expression()
    {
        return value;
    }

    public static class Deserializer extends Jrql.Deserializer<VariableAssignment>
    {
        @Override
        public VariableAssignment deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
        {
            switch (p.getCurrentToken())
            {
                case VALUE_STRING:
                    final Variable variable = ctxt.readValue(p, Variable.class);
                    return new VariableAssignment(variable, variable);

                default:
                    throw badToken(p, VALUE_STRING);
            }
        }
    }

    public static class Serializer extends JsonSerializer<VariableAssignment>
    {
        @Override
        public void serialize(VariableAssignment value, JsonGenerator gen,
                              SerializerProvider serializers) throws IOException
        {
            if (value.variable().equals(value.expression()))
                gen.writeObject(value.variable());
        }
    }
}
