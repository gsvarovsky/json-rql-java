/*
 * Copyright (c) George Svarovsky 2019. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package org.jsonrql;

import com.fasterxml.jackson.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.fasterxml.jackson.annotation.JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY;
import static com.fasterxml.jackson.annotation.JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

public class Update extends Query<Update>
{
    private final List<Subject> insert;
    private final List<Subject> delete;

    public static Update insert(Subject... insert)
    {
        return insert(asList(insert));
    }

    public static Update insert(List<Subject> insert)
    {
        return new Update(null, insert, null, emptyList());
    }

    @JsonIgnore
    public Optional<List<Subject>> insert()
    {
        return Optional.ofNullable(insert);
    }

    public Update andInsert(Subject... insert)
    {
        final List<Subject> newInsert = this.insert == null ? new ArrayList<>() : new ArrayList<>(this.insert);
        newInsert.addAll(asList(insert));
        return new Update(context, newInsert, delete, where);
    }

    public static Update delete(Subject... delete)
    {
        return delete(asList(delete));
    }

    public static Update delete(List<Subject> delete)
    {
        return new Update(null, null, delete, emptyList());
    }

    public Update andDelete(Subject... delete)
    {
        final List<Subject> newDelete = this.delete == null ? new ArrayList<>() : new ArrayList<>(this.delete);
        newDelete.addAll(asList(delete));
        return new Update(context, insert, newDelete, where);
    }

    @JsonIgnore
    public Optional<List<Subject>> delete()
    {
        return Optional.ofNullable(delete);
    }

    @Override
    public void accept(Visitor visitor)
    {
        visitor.visit(this);
    }

    @JsonCreator
    public Update(
        @JsonProperty("@context") Context context,
        @JsonProperty("@insert") @JsonFormat(with = ACCEPT_SINGLE_VALUE_AS_ARRAY) List<Subject> insert,
        @JsonProperty("@delete") @JsonFormat(with = ACCEPT_SINGLE_VALUE_AS_ARRAY) List<Subject> delete,
        @JsonProperty(value = "@where", required = true) @JsonFormat(with = ACCEPT_SINGLE_VALUE_AS_ARRAY) List<Pattern> where)
    {
        super(context, where);
        this.insert = insert == null ? null : unmodifiableList(insert);
        this.delete = delete == null ? null : unmodifiableList(delete);
    }

    @Override protected Update copyWith(Context context, List<Pattern> where)
    {
        return new Update(context, insert, delete, where);
    }

    @SuppressWarnings("unused")
    @JsonProperty("@insert")
    @JsonFormat(with = WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)
    @JsonInclude(NON_NULL)
    private List<Subject> getInsert()
    {
        return insert;
    }

    @SuppressWarnings("unused")
    @JsonProperty("@delete")
    @JsonFormat(with = WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)
    @JsonInclude(NON_NULL)
    private List<Subject> getDelete()
    {
        return delete;
    }
}
