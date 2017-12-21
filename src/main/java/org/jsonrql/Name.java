package org.jsonrql;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.util.Map;

import static java.util.Collections.singletonMap;

@JsonSerialize(using = ToStringSerializer.class)
public final class Name implements Id
{
    private final String name;

    public Name(String name)
    {
        this.name = name;
    }

    public String name()
    {
        return name;
    }

    @Override
    public void accept(Visitor visitor)
    {
        visitor.visit(this);
    }

    @Override
    public String toString()
    {
        return name;
    }

    @Override
    public Map asJsonLd()
    {
        return singletonMap("@id", name);
    }

    @Override
    public String asIRI()
    {
        return name;
    }
}
