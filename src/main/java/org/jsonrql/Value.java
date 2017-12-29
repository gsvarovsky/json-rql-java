package org.jsonrql;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.jsonrql.Literal.QualifiedLiteral;

import java.io.IOException;

import static org.jsonrql.Keywords.KEYWORDS;
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
                    return Value.value(p.getText());

                case START_OBJECT:
                    return readAhead(p, ctxt, node ->
                        fieldsOf(node).anyMatch("@value"::equals) ? QualifiedLiteral.class
                            : fieldsOf(node).anyMatch(KEYWORDS.operators::containsKey) ? InlineFilter.class
                            : Subject.class);

                default:
                    return ctxt.readValue(p, Literal.class);
            }
        }
    }

    static Value value(String str)
    {
        // Note that we can be explicit about @ids and @values using an object
        return str == null ? null : matchVar(str).map(Value.class::cast).orElse(literal(str));
    }
}