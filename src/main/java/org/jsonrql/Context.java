/*
 * Copyright (c) George Svarovsky 2019. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package org.jsonrql;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.core.JsonToken.*;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toMap;
import static org.jsonrql.Name.name;

@SuppressWarnings({"unused", "WeakerAccess"})
public final class Context
{
    private static final Context EMPTY_CONTEXT = new Context(null, null, null, emptyMap());
    private final String language;
    private final Name base;
    private final Name vocab;
    private final Map<Name, TermDef> names;

    public enum ContainerType
    {
        @JsonProperty("@list") LIST,
        @JsonProperty("@set") SET,
        @JsonProperty("@language") LANGUAGE,
        @JsonProperty("@index") INDEX;

        public String tag()
        {
            return "@" + name().toLowerCase();
        }
    }

    @JsonDeserialize(using = Context.TermDef.Deserializer.class)
    public static abstract class TermDef
    {
        protected final Name id;

        TermDef(Name id)
        {
            this.id = id;
        }

        public Optional<Name> id()
        {
            return Optional.ofNullable(id);
        }

        public Optional<TermDef> reverse()
        {
            return Optional.empty();
        }

        public Optional<Name> type()
        {
            return Optional.empty();
        }

        public Optional<String> language()
        {
            return Optional.empty();
        }

        public Optional<ContainerType> container()
        {
            return Optional.empty();
        }

        public abstract TermDef id(Name id);

        public abstract TermDef reverse(TermDef reverse);

        public abstract TermDef type(Name type);

        public abstract TermDef language(String language);

        public abstract TermDef container(ContainerType container);

        public TermDef isId()
        {
            return type(name("@id"));
        }

        public static class Deserializer extends Jrql.Deserializer<TermDef>
        {
            @Override
            public TermDef deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
            {
                switch (p.getCurrentToken())
                {
                    case START_OBJECT:
                        return ctxt.readValue(p, ExpandedTermDef.class);
                    case VALUE_STRING:
                        return ctxt.readValue(p, TermId.class);
                    case VALUE_NULL:
                        return null;
                    default:
                        throw badToken(p, START_OBJECT, VALUE_STRING, VALUE_NULL);
                }
            }
        }
    }

    @JsonDeserialize
    public static final class TermId extends TermDef
    {
        @Override public TermDef id(Name id)
        {
            return new TermId(id);
        }

        @Override public TermDef reverse(TermDef reverse)
        {
            assert false;
            return new ExpandedTermDef(null, reverse, null, null, null);
        }

        @Override public TermDef type(Name type)
        {
            return new ExpandedTermDef(id, null, type, null, null);
        }

        @Override public TermDef language(String language)
        {
            return new ExpandedTermDef(id, null, null, language, null);
        }

        @Override public TermDef container(ContainerType container)
        {
            return new ExpandedTermDef(id, null, null, null, container);
        }

        @JsonCreator private TermId(Name id)
        {
            super(id);
        }

        @JsonValue public Name getId()
        {
            return id;
        }
    }

    @JsonDeserialize
    public static final class ExpandedTermDef extends TermDef
    {
        private final TermDef reverse;
        private final Name type;
        private final String language;
        private final ContainerType container;

        @Override public TermDef id(Name id)
        {
            assert reverse == null;
            return new ExpandedTermDef(id, null, type, language, container);
        }

        @Override public TermDef reverse(TermDef reverse)
        {
            assert id == null;
            return new ExpandedTermDef(null, reverse, type, language, container);
        }

        @Override public TermDef type(Name type)
        {
            return new ExpandedTermDef(id, reverse, type, language, container);
        }

        @Override public TermDef language(String language)
        {
            return new ExpandedTermDef(id, reverse, type, language, container);
        }

        @Override public TermDef container(ContainerType container)
        {
            return new ExpandedTermDef(id, reverse, type, language, container);
        }

        @JsonCreator
        private ExpandedTermDef(@JsonProperty("@id") Name id,
                                @JsonProperty("@reverse") TermDef reverse,
                                @JsonProperty("@type") Name type,
                                @JsonProperty("@language") String language,
                                @JsonProperty("@container") ContainerType container)
        {
            super(id);
            assert reverse == null || container == null ||
                container == ContainerType.SET || container == ContainerType.INDEX;

            this.reverse = reverse;
            this.type = type;
            this.language = language;
            this.container = container;
        }

        @JsonProperty("@id")
        @JsonInclude(NON_NULL)
        public Name getId()
        {
            return id;
        }

        @JsonProperty("@reverse")
        @JsonInclude(NON_NULL)
        public TermDef getReverse()
        {
            return reverse;
        }

        @JsonProperty("@type")
        @JsonInclude(NON_NULL)
        public Name getType()
        {
            return type;
        }

        @JsonProperty("@language")
        @JsonInclude(NON_NULL)
        public String getLanguage()
        {
            return language;
        }

        @JsonProperty("@container")
        @JsonInclude(NON_NULL)
        public ContainerType getContainer()
        {
            return container;
        }
    }

    public static Context context()
    {
        return EMPTY_CONTEXT;
    }

    public static TermDef termDef(Name id)
    {
        return new TermId(id);
    }

    public static TermDef termDef(String id)
    {
        return termDef(name(id));
    }

    public Context language(String language)
    {
        return new Context(language, base, vocab, names);
    }

    public Optional<String> language()
    {
        return Optional.ofNullable(language);
    }

    public Context base(Name base)
    {
        return new Context(language, base, vocab, names);
    }

    public Optional<Name> base()
    {
        return Optional.ofNullable(base);
    }

    public Context vocab(Name vocab)
    {
        return new Context(language, base, vocab, names);
    }

    public Optional<Name> vocab()
    {
        return Optional.ofNullable(vocab);
    }

    public Context with(String name, TermDef termDef)
    {
        final Context context = new Context(language, base, vocab, names);
        context.names.put(name(name), termDef);
        return context;
    }

    public Context prefix(String name, String id)
    {
        return with(name, termDef(id));
    }

    public Context with(Context other)
    {
        Context context = new Context(language, base, vocab, names);
        if (other.base != null)
            context = context.base(other.base);
        if (other.vocab != null)
            context = context.vocab(other.vocab);
        if (other.language != null)
            context = context.language(other.language);
        context.names.putAll(other.names);
        return context;
    }

    public Context without(Name name)
    {
        if (names.containsKey(name))
        {
            final Context context = new Context(language, base, vocab, names);
            context.names.remove(name);
            return context;
        }
        return this;
    }

    public Context without(String name)
    {
        return without(name(name));
    }

    public Context without(Context other)
    {
        Context context = new Context(language, base, vocab, names);
        if (other.base != null && other.base.equals(base))
            context = context.base(null);
        if (other.vocab != null && other.vocab.equals(vocab))
            context = context.vocab(null);
        if (other.language != null && other.language.equals(language))
            context = context.language(null);
        context.names.entrySet().removeAll(other.names.entrySet());
        return context;
    }

    public Map<String, String> prefixes()
    {
        final Map<String, String> prefixes = new HashMap<>(
            names.entrySet().stream().filter(e -> e.getValue().id().isPresent())
                .collect(toMap(e -> e.getKey().toString(),
                               e -> e.getValue().id().orElseThrow(AssertionError::new).toString())));
        if (vocab != null)
            prefixes.put("", vocab.toString());
        return prefixes;
    }

    public Name resolve(Name name)
    {
        return base != null ? Name.name(URI.create(base.toString()).resolve(name.toString()).toString()) : name;
    }

    public Map<Name, TermDef> names()
    {
        return unmodifiableMap(names);
    }

    @JsonIgnore public boolean isEmpty()
    {
        return language == null && base == null && vocab == null && names.isEmpty();
    }

    @JsonCreator private Context(@JsonProperty("@language") String language,
                                 @JsonProperty("@base") Name base,
                                 @JsonProperty("@vocab") Name vocab)
    {
        this(language, base, vocab, emptyMap());
    }

    private Context(String language,
                    Name base,
                    Name vocab,
                    Map<Name, TermDef> names)
    {
        this.language = language;
        this.base = base;
        this.vocab = vocab;
        this.names = new HashMap<>(names);
    }

    @JsonProperty("@language")
    @JsonInclude(NON_NULL)
    private String getLanguage()
    {
        return language;
    }

    @JsonProperty("@base")
    @JsonInclude(NON_NULL)
    private Name getBase()
    {
        return base;
    }

    @JsonProperty("@vocab")
    @JsonInclude(NON_NULL)
    private Name getVocab()
    {
        return vocab;
    }

    @JsonAnyGetter
    private Map<Name, TermDef> getNames()
    {
        return names;
    }

    @JsonAnySetter
    private void setTermDef(String name, TermDef termDef)
    {
        names.put(name(name), termDef);
    }
}
