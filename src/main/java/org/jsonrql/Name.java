package org.jsonrql;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
public final class Name implements Id
{
    private final String name;

    public static Name name(String name)
    {
        return new Name(name);
    }

    @JsonCreator
    private Name(String name)
    {
        this.name = name;
    }

    @Override
    public void accept(Visitor visitor)
    {
        visitor.visit(this);
    }

    @Override
    @JsonValue
    public String toString()
    {
        return name;
    }
}
