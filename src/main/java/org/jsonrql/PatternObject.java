package org.jsonrql;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static java.util.Collections.unmodifiableMap;

@JsonDeserialize
public final class PatternObject implements Pattern, Value
{
    private final Id id;
    private final Id type;
    private final Map<Id, Value> properties = new HashMap<>();

    public static PatternObject POBJ = new PatternObject(null, null);

    @Override
    public void accept(Visitor visitor)
    {
        visitor.visit(this);
    }

    public Optional<Id> id()
    {
        return Optional.ofNullable(id);
    }

    public PatternObject id(Id id)
    {
        return new PatternObject(id, type, properties);
    }

    public PatternObject id(String id)
    {
        return id(Id.from(id));
    }

    public Optional<Id> type()
    {
        return Optional.ofNullable(type);
    }

    public PatternObject type(Id type)
    {
        return new PatternObject(id, type, properties);
    }

    public PatternObject with(String key, String value)
    {
        return new PatternObject(this, Id.from(key), Value.from(value));
    }

    @Override
    public Map asJsonLd()
    {
        final Map<String, Object> jsonld = new HashMap<>();
        id().ifPresent(id -> jsonld.put("@id", id.asIRI()));
        type().ifPresent(type -> jsonld.put("@type", type.asIRI()));
        properties.forEach((identifier, value) -> jsonld.put(identifier.asIRI(),
                                                             value == null ? null : value.asJsonLd()));
        return jsonld;
    }

    @JsonCreator
    private PatternObject(@JsonProperty("@id") Id id, @JsonProperty("@type") Id type)
    {
        this.id = id;
        this.type = type;
    }

    private PatternObject(Id id, Id type, Map<Id, Value> properties)
    {
        this.id = id;
        this.type = type;
        this.properties.putAll(properties);
    }

    private PatternObject(PatternObject pobj, Id newId, Value newValue)
    {
        this.id = pobj.id;
        this.type = pobj.type;
        this.properties.putAll(pobj.properties);
        this.properties.put(newId, newValue);
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
    @SuppressWarnings("unused")
    public Map<Id, Value> properties()
    {
        return unmodifiableMap(properties);
    }

    @JsonAnySetter
    @SuppressWarnings("unused")
    private void setProperty(String key, Value expr)
    {
        properties.put(Id.from(key), expr);
    }
}
