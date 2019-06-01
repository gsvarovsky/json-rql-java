/*
 * Copyright (c) George Svarovsky 2019. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package org.jsonrql;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.jsonldjava.utils.JsonUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

import static com.fasterxml.jackson.annotation.JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY;
import static com.fasterxml.jackson.annotation.JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static java.util.Arrays.stream;
import static java.util.Collections.*;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

@SuppressWarnings("WeakerAccess") @JsonDeserialize
public final class Subject extends Pattern implements Value
{
    private final Id id;
    private final Id type;
    private final Map<Id, List<Value>> properties;

    public static Subject subject(Id id)
    {
        return new Subject((Context)null, id, null);
    }

    public static Subject subject(String id)
    {
        return subject(Id.id(id));
    }

    @Override public Subject context(Context context)
    {
        return new Subject(context, id, type, properties);
    }

    @Override
    public void accept(Visitor visitor)
    {
        visitor.visit(this);
    }

    public Optional<Id> id()
    {
        return Optional.ofNullable(id);
    }

    public Subject id(Id id)
    {
        return new Subject(context, id, type, properties);
    }

    public Optional<Id> type()
    {
        return Optional.ofNullable(type);
    }

    public Subject type(Id type)
    {
        return new Subject(context, id, type, properties);
    }

    public Subject type(String type)
    {
        return type(Id.id(type));
    }

    public Subject with(String key, String... values)
    {
        return new Subject(this, Id.id(key), stream(values).map(Value::value));
    }

    public Subject with(String key, Value... values)
    {
        return new Subject(this, Id.id(key), stream(values));
    }

    public Subject with(Id key, Value... values)
    {
        return new Subject(this, key, stream(values));
    }

    public Stream<Value> values()
    {
        return properties.values().stream().flatMap(List::stream);
    }

    public Optional<List<Value>> get(String key)
    {
        return Optional.ofNullable(properties.get(Id.id(key)));
    }

    public Optional<Value> getValue(String key)
    {
        return get(key).map(l -> {
            if (l.size() == 1)
                return l.get(0);
            else
                throw new IndexOutOfBoundsException();
        });
    }

    public Optional<Literal> getLiteral(String key)
    {
        return getValue(key).map(Literal.class::cast);
    }

    public Optional<String> getString(String key)
    {
        return getLiteral(key).map(Literal::value).map(String.class::cast);
    }

    @JsonCreator
    private Subject(@JsonProperty("@context") Context context,
                    @JsonProperty("@id") Id id,
                    @JsonProperty("@type") Id type)
    {
        this(context, id, type, emptyMap());
    }

    private Subject(Subject pobj, Id newId, Stream<Value> newValues)
    {
        this(pobj.context, pobj.id, pobj.type, pobj.properties);
        properties.computeIfPresent(newId, (id, values) -> valuesList(concat(values.stream(), newValues)));
        properties.computeIfAbsent(newId, id -> valuesList(newValues));
    }

    private Subject(Context context, Id id, Id type, Map<Id, List<Value>> properties)
    {
        super(context);
        this.id = id;
        this.type = type;
        this.properties = new HashMap<>(properties);
    }

    @JsonProperty("@id")
    @JsonInclude(NON_NULL)
    @SuppressWarnings("unused")
    private Value getId()
    {
        return id;
    }

    @JsonProperty("@type")
    @JsonInclude(NON_NULL)
    @SuppressWarnings("unused")
    private Value getType()
    {
        return type;
    }

    @JsonAnyGetter
    @JsonFormat(with = WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)
    @SuppressWarnings("unused")
    public Map<Id, List<Value>> properties()
    {
        return unmodifiableMap(properties);
    }

    @JsonAnySetter
    @JsonFormat(with = ACCEPT_SINGLE_VALUE_AS_ARRAY)
    @SuppressWarnings("unused")
    private void setProperty(String key, List<Value> expr)
    {
        properties.put(Id.id(key), unmodifiableList(expr));
    }

    private static List<Value> valuesList(Stream<Value> newValues)
    {
        return unmodifiableList(newValues.collect(toList()));
    }

    @Override
    public String toString()
    {
        try
        {
            return JsonUtils.toPrettyString(this);
        }
        catch (IOException e)
        {
            throw new AssertionError(e);
        }
    }

    @Override
    public boolean equals(Object o)
    {
        return this == o || o instanceof Subject &&
            Objects.equals(id, ((Subject)o).id) &&
            Objects.equals(type, ((Subject)o).type) &&
            Objects.equals(properties, ((Subject)o).properties) &&
            Objects.equals(context, ((Subject)o).context);
    }

    @Override
    public int hashCode()
    {

        return Objects.hash(id, type, properties);
    }
}
