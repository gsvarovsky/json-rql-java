package org.jsonrql;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Map;

import static java.util.Collections.singletonMap;

@JsonSerialize
public final class Name implements Id
{
    private final String name;

    @JsonCreator
    public Name(String name)
    {
        this.name = name;
    }

    @JsonValue
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
