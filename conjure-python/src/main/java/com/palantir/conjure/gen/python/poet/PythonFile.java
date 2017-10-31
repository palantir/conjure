/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.python.poet;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import java.util.List;
import java.util.Set;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface PythonFile extends Emittable {

    /*
     * This file will lie at packageName/__init__.py i.e. there is only one python file for a given package name.
     */
    String packageName();

    Set<PythonImport> imports();

    List<PythonClass> contents();

    @Override
    default void emit(PythonPoetWriter poetWriter) {
        poetWriter.maintainingIndent(() -> {
            poetWriter.writeLine(String.format("# this is package %s", packageName()));
            imports().stream().sorted().forEach(poetWriter::emit);
            poetWriter.writeLine();
            contents().forEach(poetWriter::emit);
        });
    }

    class Builder extends ImmutablePythonFile.Builder {}

    static Builder builder() {
        return new Builder();
    }

}
