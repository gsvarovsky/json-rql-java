package org.jsonrql;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;
import java.util.Optional;

import static com.fasterxml.jackson.annotation.JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY;
import static com.fasterxml.jackson.annotation.JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonDeserialize
public final class Group implements Pattern
{
    private final List<PatternObject> graph;
    private final List<Expression> filter;

    @JsonIgnore
    public Optional<List<PatternObject>> graph()
    {
        return Optional.ofNullable(graph);
    }

    @JsonIgnore
    public Optional<List<Expression>> filter()
    {
        return Optional.ofNullable(filter);
    }

    @JsonCreator
    private Group(@JsonProperty("@graph") @JsonFormat(with = ACCEPT_SINGLE_VALUE_AS_ARRAY) List<PatternObject> graph,
                  @JsonProperty("@filter") @JsonFormat(with = ACCEPT_SINGLE_VALUE_AS_ARRAY) List<Expression> filter)
    {
        this.graph = graph;
        this.filter = filter;
    }

    @SuppressWarnings("unused")
    @JsonProperty("@graph")
    @JsonFormat(with = WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)
    @JsonInclude(NON_NULL)
    private List<PatternObject> getGraph()
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
