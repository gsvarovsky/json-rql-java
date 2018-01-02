package org.jsonrql;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static com.fasterxml.jackson.core.JsonToken.START_OBJECT;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toMap;
import static org.jsonrql.Keywords.KEYWORDS;

@JsonDeserialize(using = Pattern.Deserializer.class)
public abstract class Pattern implements Jrql
{
    private final Map<String, Object> context;

    public abstract Pattern context(Map<String, Object> context);

    @SafeVarargs
    public static Map<String, Object> newContext(Consumer<Map<String, Object>>... modify)
    {
        return newContext(emptyMap(), modify);
    }

    @SafeVarargs
    public static Map<String, Object> newContext(Map<String, Object> context, Consumer<Map<String, Object>>... modify)
    {
        final Map<String, Object> newContext = new HashMap<>(context);
        stream(modify).forEach(m -> m.accept(newContext));
        return newContext;
    }

    @SafeVarargs
    public final Map<String, Object> contextWith(Consumer<Map<String, Object>>... modify)
    {
        return newContext(context, modify);
    }

    @JsonProperty("@context")
    @JsonInclude(NON_EMPTY)
    public Map<String, Object> context()
    {
        return context;
    }

    public static Consumer<Map<String, Object>> base(String base)
    {
        return context -> context.put("@base", base);
    }

    public static Consumer<Map<String, Object>> vocab(String vocab)
    {
        return context -> context.put("@vocab", vocab);
    }

    public static Consumer<Map<String, Object>> prefix(String pre, String expanded)
    {
        return context -> context.put(pre, expanded);
    }

    @JsonIgnore
    public Map<String, String> prefixes()
    {
        return context.entrySet().stream()
            .filter(e -> e.getValue() instanceof String && !"@base".equals(e.getKey()))
            .collect(toMap(
                e -> "@vocab".equals(e.getKey()) ? "" : e.getKey(),
                e -> e.getValue().toString()));
    }

    public Name resolve(Name name)
    {
        return resolve(context, name);
    }

    public static Name resolve(Map<String, Object> context, Name name)
    {
        return context.containsKey("@base") ?
            Name.name(URI.create(context.get("@base").toString()).resolve(name.toString()).toString()) : name;
    }

    Pattern(Map<String, Object> context)
    {
        this.context = context == null ? emptyMap() : unmodifiableMap(context);
    }

    static class Deserializer extends Jrql.Deserializer<Pattern>
    {
        @Override
        public Pattern deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
        {
            switch (p.getCurrentToken())
            {
                case START_OBJECT:
                    // Unfortunately we need to read ahead to decide on the target type
                    return readAhead(p, ctxt, node ->
                        fieldsOf(node).anyMatch(KEYWORDS.clauses::containsKey) ? Query.class
                            : fieldsOf(node).anyMatch(KEYWORDS.groupPatterns::containsKey) ? Group.class
                            : Subject.class);

                default:
                    throw badToken(p, START_OBJECT);
            }
        }
    }
}
