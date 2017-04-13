/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.python.poet;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.types.names.ConjurePackage;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface PythonClassName extends Comparable<PythonClassName> {

    ConjurePackage conjurePackage();

    String className();

    static PythonClassName of(ConjurePackage conjurePackage, String className) {
        return ImmutablePythonClassName.builder()
                .conjurePackage(conjurePackage)
                .className(className)
                .build();
    }

    @Override
    default int compareTo(PythonClassName other) {
        int comparePackageName = conjurePackage().name().compareTo(other.conjurePackage().name());
        if (comparePackageName != 0) {
            return comparePackageName;
        }
        return className().compareTo(other.className());
    }

}
