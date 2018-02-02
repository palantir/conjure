/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.python.poet;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface PythonAlias extends PythonClass {
    String aliasName();
    String aliasTarget();

    @Override
    default void emit(PythonPoetWriter poetWriter) {
        poetWriter.writeIndentedLine(String.format("%s = %s", aliasName(), aliasTarget()));
        poetWriter.writeLine();
    }

    class Builder extends ImmutablePythonAlias.Builder {}

    static Builder builder() {
        return new Builder();
    }
}
