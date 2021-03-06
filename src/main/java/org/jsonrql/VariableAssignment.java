/*
 * Copyright (c) George Svarovsky 2019. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package org.jsonrql;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize
public final class VariableAssignment implements Result
{
    private final Variable variable;
    private final Expression expression;

    public VariableAssignment(Variable variable, Expression expression)
    {
        this.variable = variable;
        this.expression = expression;
    }

    @Override
    public void accept(Visitor visitor)
    {
        visitor.visit(this);
    }

    public Variable variable()
    {
        return variable;
    }

    public Expression expression()
    {
        return expression;
    }
}
