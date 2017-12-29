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
public final class PatternObject implements Pattern, Value
{
    private final Id subject;
    private final Id type;
    private final Map<Id, List<Value>> properties;

    @Override
    public void accept(Visitor visitor)
    {
        visitor.visit(this);
    }

    public Optional<Id> subject()
    {
        return Optional.ofNullable(subject);
    }

    public static PatternObject subject(Id id)
    {
        return new PatternObject(id, null);
    }

    public static PatternObject subject(String id)
    {
        return subject(Id.id(id));
    }

    public Optional<Id> type()
    {
        return Optional.ofNullable(type);
    }

    public PatternObject type(Id type)
    {
        return new PatternObject(subject, type, properties);
    }

    public PatternObject type(String type)
    {
        return type(Id.id(type));
    }

    public PatternObject with(String key, String... values)
    {
        return new PatternObject(this, Id.id(key), stream(values).map(Value::value));
    }

    public PatternObject with(String key, Value... values)
    {
        return new PatternObject(this, Id.id(key), stream(values));
    }

    public Stream<Value> values()
    {
        return properties.values().stream().flatMap(List::stream);
    }

    @JsonCreator
    private PatternObject(@JsonProperty("@id") Id subject, @JsonProperty("@type") Id type)
    {
        this(subject, type, emptyMap());
    }

    private PatternObject(PatternObject pobj, Id newId, Stream<Value> newValues)
    {
        this(pobj.subject, pobj.type, pobj.properties);
        properties.computeIfPresent(newId, (id, values) -> valuesList(concat(values.stream(), newValues)));
        properties.computeIfAbsent(newId, id -> valuesList(newValues));
    }

    private PatternObject(Id subject, Id type, Map<Id, List<Value>> properties)
    {
        this.subject = subject;
        this.type = type;
        this.properties = new HashMap<>(properties);
    }

    @JsonProperty("@id")
    @JsonInclude(NON_NULL)
    @SuppressWarnings("unused")
    private Value getSubject()
    {
        return subject;
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
