/*
 * Copyright (c) George Svarovsky 2019. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package org.jsonrql.jena;

import org.apache.jena.sparql.modify.request.QuadAcc;
import org.apache.jena.sparql.modify.request.UpdateDeleteWhere;
import org.apache.jena.sparql.modify.request.UpdateModify;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import org.jsonrql.Subject;

import java.util.List;

import static org.jsonrql.jena.JsonRqlJena.asPattern;
import static org.jsonrql.jsonld.JsonLd.asGraph;

public class JsonRqlJenaUpdateBuilder extends JsonRqlJenaBuilder<UpdateRequest>
{
    private final UpdateRequest updateRequest = UpdateFactory.create();

    private final org.jsonrql.Update jrqlUpdate;

    JsonRqlJenaUpdateBuilder(org.jsonrql.Update jrqlUpdate)
    {
        super(jrqlUpdate);
        this.jrqlUpdate = jrqlUpdate;
    }

    @Override public UpdateRequest build()
    {
        final Update update;
        final QuadAcc insert, delete;
        if (jrqlUpdate.delete().isPresent() && !jrqlUpdate.insert().isPresent() && jrqlUpdate.where().isEmpty())
        {
            // DELETE WHERE {}
            insert = null;
            delete = new QuadAcc();
            update = new UpdateDeleteWhere(delete);
        }
        else
        {
            // DELETE {} INSERT {} WHERE {}
            final UpdateModify updateModify = new UpdateModify();
            insert = updateModify.getInsertAcc();
            delete = updateModify.getDeleteAcc();
            whereElement(false).ifPresent(updateModify::setElement);
            update = updateModify;
        }

        jrqlUpdate.delete().ifPresent(subjects -> accTriples(subjects, delete));
        jrqlUpdate.insert().ifPresent(subjects -> accTriples(subjects, insert));

        updateRequest.add(update);
        return updateRequest;
    }

    private void accTriples(List<Subject> subjects, QuadAcc triples)
    {
        subjects.forEach(subject -> asPattern(asGraph(subject), ctx).forEach(triples::addTriple));
    }
}
