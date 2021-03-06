/*
 * Copyright 2016 ITEA 12004 SEAS Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.thesmartenergy.sparql.generate.jena.iterator;

import java.util.ArrayList;
import java.util.List;
import org.apache.jena.query.QueryBuildException;
import org.apache.jena.sparql.ARQInternalErrorException;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.util.Context;

/**
 * The base implementation of the {@link IteratorFunction} interface.
 */
public abstract class IteratorFunctionBase implements IteratorFunction {

    /** The list of argument expressions. */
    protected ExprList arguments = null;
    /** The function environment. */
    private FunctionEnv env;

    /**
     * Build a iterator function execution with the given arguments,
     * and operate a
     * check of the build.
     * @param args -
     * @throws QueryBuildException if the iterator function cannot be
     * executed with the
     * given arguments.
     */
    @Override
    public final void build(ExprList args) {
        arguments = args;
        checkBuild(args);
    }

    /**
     * Partially checks if the iterator function can be executed with the given
     * arguments.
     * @param args -
     * @throws QueryBuildException if the iterator function cannot be executed with the
     * given arguments.
     */
    public abstract void checkBuild(ExprList args);

    @Override
    public List<NodeValue> exec(
            Binding binding, ExprList args, FunctionEnv env) {
        this.env = env;
        if (args == null) {
            throw new ARQInternalErrorException("IteratorFunctionBase:"
                    + " Null args list");
        }

        List<NodeValue> evalArgs = new ArrayList<>();
        for (Expr e : args) {
            NodeValue x = e.eval(binding, env);
            evalArgs.add(x);
        }

        List<NodeValue> nv = exec(evalArgs);
        arguments = null;
        return nv;
    }

    /**
     * Return the Context object for this execution.
     * @return -
     */
    public Context getContext() {
        return env.getContext();
    }

    /**
     * IteratorFunction call to a list of evaluated argument values.
     * @param args -
     * @return -
     */
    public abstract List<NodeValue> exec(List<NodeValue> args);

}
