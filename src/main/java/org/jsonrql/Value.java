package org.jsonrql;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.jsonrql.Literal.PlainLiteral;
import org.jsonrql.Literal.QualifiedLiteral;

import java.io.IOException;

import static com.fasterxml.jackson.core.JsonToken.START_OBJECT;
import static com.fasterxml.jackson.core.JsonToken.VALUE_STRING;
import static org.jsonrql.Literal.literal;
import static org.jsonrql.Variable.matchVar;

@JsonDeserialize(using = Value.Deserializer.class)
public interface Value extends Jrql
{
    class Deserializer extends Jrql.Deserializer<Value>
    {
        @Override
        public Value deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
        {
            switch (p.getCurrentToken())
            {
                case VALUE_STRING:
                    return Value.from(p.getText());

                case START_OBJECT:
                    return readAhead(p, ctxt, node ->
                        // TODO: In-line filters will go here
                        fieldsOf(node).anyMatch("@value"::equals) ? QualifiedLiteral.class : PatternObject.class);

                case VALUE_NUMBER_INT:
                case VALUE_NUMBER_FLOAT:
                case VALUE_TRUE:
                case VALUE_FALSE:
                    return ctxt.readValue(p, PlainLiteral.class);

                case VALUE_NULL:
                    return null;

                default:
                    throw badToken(p, VALUE_STRING, START_OBJECT);
            }
        }
    }

    Object asJsonLd();

    static Value from(String str)
    {
        // Note that we can be explicit about @ids and @values using an object
        return matchVar(str).map(Value.class::cast).orElse(literal(str));
    }
}