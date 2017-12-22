package org.jsonrql;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;

import static com.fasterxml.jackson.core.JsonToken.VALUE_STRING;

@JsonDeserialize(using = Result.Deserializer.class)
public interface Result extends Jrql
{
    final class Star implements Result
    {
        @Override
        public void accept(Visitor visitor)
        {
            visitor.visit(this);
        }

        @Override
        @JsonValue
        public String toString()
        {
            return "*";
        }
    }
    Result STAR = new Star();

    class Deserializer extends Jrql.Deserializer<Result>
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
