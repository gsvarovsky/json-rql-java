package org.jsonrql;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.io.IOException;

import static com.fasterxml.jackson.core.JsonToken.VALUE_STRING;

@JsonDeserialize(using = Result.Deserializer.class)
public interface Result extends Jrql
{
    @JsonSerialize(using = ToStringSerializer.class)
    final class Star implements Result
    {
        @Override
        public void accept(Visitor visitor)
        {
            visitor.visit(this);
        }

        @Override
        public String toString()
        {
            return "*";
        }
    }
    public Result STAR = new Star();

    class Deserializer extends JsonDeserializer<Result>
    {
        @Override
        public Result deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
        {
            if (p.getCurrentToken() == VALUE_STRING && STAR.toString().equals(p.getText()))
                return STAR;
            else
                return ctxt.readValue(p, VariableAssignment.class);
        }
    }
}
