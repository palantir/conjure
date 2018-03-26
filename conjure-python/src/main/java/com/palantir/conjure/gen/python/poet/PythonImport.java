/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.python.poet;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface PythonImport extends Emittable, Comparable<PythonImport> {

    PythonClassName className();

    Optional<String> relativeToPackage();

    static PythonImport of(PythonClassName className, Optional<String> relativeToPackage) {
        return ImmutablePythonImport.builder().className(className).relativeToPackage(relativeToPackage).build();
    }

    @Override
    default void emit(PythonPoetWriter poetWriter) {
        // TODO (bduffield): emit relative to package
        poetWriter.writeIndentedLine(String.format("from %s import %s",
                className().conjurePackage(), className().className()));
    }

    @Override
    default int compareTo(PythonImport other) {
        return className().compareTo(other.className());
    }

}
