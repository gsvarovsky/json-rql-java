/*
 * Copyright (c) George Svarovsky 2020. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package org.jsonrql;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.fasterxml.jackson.annotation.JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY;
import static com.fasterxml.jackson.annotation.JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static java.util.Arrays.asList;

@JsonDeserialize
public final class Group extends Pattern
{
    private final List<Subject> graph;
    private final List<Expression> filter;
    protected final List<Pattern> union;

    public static Group group(List<Subject> graph)
    {
        return new Group(null, graph, null, null);
    }

    public static Group group(Subject... graph)
    {
        return group(asList(graph));
    }

    public static Group union(List<Pattern> union)
    {
        return new Group(null, null, null, union);
    }

    public static Group union(Pattern... union)
    {
        return union(asList(union));
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

    @JsonIgnore
    public Optional<List<Pattern>> union()
    {
        return Optional.ofNullable(union);
    }

    public Group filter(List<Expression> filter)
    {
        return new Group(context, graph, filter, union);
    }

    public Group filter(Expression... filter)
    {
        return filter(asList(filter));
    }

    @Override public Group context(Context context)
    {
        return new Group(context, graph, filter, union);
    }

    @JsonCreator
    private Group(@JsonProperty("@context") Context context,
                  @JsonProperty("@graph") @JsonFormat(with = ACCEPT_SINGLE_VALUE_AS_ARRAY) List<Subject> graph,
                  @JsonProperty("@filter") @JsonFormat(with = ACCEPT_SINGLE_VALUE_AS_ARRAY) List<Expression> filter,
                  @JsonProperty("@union") @JsonFormat(with = ACCEPT_SINGLE_VALUE_AS_ARRAY) List<Pattern> union)
    {
        super(context);
        this.graph = graph == null ? null : new ArrayList<>(graph);
        this.filter = filter == null ? null : new ArrayList<>(filter);
        this.union = union == null ? null : new ArrayList<>(union);
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

    @JsonProperty("@union")
    @JsonFormat(with = WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)
    @JsonInclude(NON_NULL)
    public List<Pattern> getUnion()
    {
        return union;
    }

    @Override
    public void accept(Visitor visitor)
    {
        visitor.visit(this);
    }
}
