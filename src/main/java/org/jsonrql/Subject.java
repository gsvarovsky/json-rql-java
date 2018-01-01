package org.jsonrql;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.fasterxml.jackson.annotation.JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY;
import static com.fasterxml.jackson.annotation.JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static java.util.Arrays.stream;
import static java.util.Collections.*;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

@JsonDeserialize
public final class Subject extends Pattern implements Value
{
    private final Id id;
    private final Id type;
    private final Map<Id, List<Value>> properties;

    public static Subject subject(Id id)
    {
        return new Subject(emptyMap(), id, null);
    }

    public static Subject subject(String id)
    {
        return subject(Id.id(id));
    }

    @Override
    public final Subject context(Map<String, Object> context)
    {
        return new Subject(context, id, type);
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

    public Optional<Id> type()
    {
        return Optional.ofNullable(type);
    }

    public Subject type(Id type)
    {
        return new Subject(context(), id, type, properties);
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

    public Stream<Value> values()
    {
        return properties.values().stream().flatMap(List::stream);
    }

    @JsonCreator
    private Subject(@JsonProperty("@context") Map<String, Object> context,
                    @JsonProperty("@id") Id id,
                    @JsonProperty("@type") Id type)
    {
        this(context, id, type, emptyMap());
    }

    private Subject(Subject pobj, Id newId, Stream<Value> newValues)
    {
        this(pobj.context(), pobj.id, pobj.type, pobj.properties);
        properties.computeIfPresent(newId, (id, values) -> valuesList(concat(values.stream(), newValues)));
        properties.computeIfAbsent(newId, id -> valuesList(newValues));
    }

    private Subject(Map<String, Object> context, Id id, Id type, Map<Id, List<Value>> properties)
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
}
