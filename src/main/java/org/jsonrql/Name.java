/*
 * Copyright (c) George Svarovsky 2019. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package org.jsonrql;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o)
    {
        return this == o || o instanceof Name && Objects.equals(name, ((Name) o).name);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name);
    }
}
