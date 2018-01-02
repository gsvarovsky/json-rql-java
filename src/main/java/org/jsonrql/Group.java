package org.jsonrql;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static com.fasterxml.jackson.annotation.JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY;
import static com.fasterxml.jackson.annotation.JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

@JsonDeserialize
public final class Group extends Pattern
{
    private final List<Subject> graph;
    private final List<Expression> filter;

    public static Group group(List<Subject> graph)
    {
        return new Group(null, graph, null);
    }

    public static Group group(Subject... graph)
    {
        return group(asList(graph));
    }

    @SafeVarargs
    public final Group context(Consumer<Map<String, Object>>... modify)
    {
        return context(contextWith(modify));
    }

    @Override
    public final Group context(Map<String, Object> context)
    {
        return new Group(context, graph, filter);
    }

    @JsonIgnore
    public Optional<List<Subject>> graph()
    {
        return Optional.ofNullable(graph);
    }

    @JsonIgnore
    public Optional<List<Expression>> filter()
    {
        return Optional.ofNullable(filter);
    }

    public Group filter(List<Expression> filter) {
        return new Group(context(), graph, filter);
    }

    public Group filter(Expression... filter) {
        return filter(asList(filter));
    }

    @JsonCreator
    private Group(@JsonProperty("@context") Map<String, Object> context,
                  @JsonProperty("@graph") @JsonFormat(with = ACCEPT_SINGLE_VALUE_AS_ARRAY) List<Subject> graph,
                  @JsonProperty("@filter") @JsonFormat(with = ACCEPT_SINGLE_VALUE_AS_ARRAY) List<Expression> filter)
    {
        super(context);
        this.graph = graph == null ? null : unmodifiableList(graph);
        this.filter = filter == null ? null : unmodifiableList(filter);
    }

    @SuppressWarnings("unused")
    @JsonProperty("@graph")
    @JsonFormat(with = WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)
    @JsonInclude(NON_NULL)
    private List<Subject> getGraph()
    {
        return graph;
    }

    @SuppressWarnings("unused")
    @JsonProperty("@filter")
    @JsonFormat(with = WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)
    @JsonInclude(NON_NULL)
    private List<Expression> getFilter()
    {
        return filter;
    }

    @Override
    public void accept(Visitor visitor)
    {
        visitor.visit(this);
    }
}
