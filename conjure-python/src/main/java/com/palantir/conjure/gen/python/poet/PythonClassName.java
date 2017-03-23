/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.python.poet;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface PythonClassName extends Comparable<PythonClassName> {

    String packageName();

    String className();

    static PythonClassName of(String packageName, String className) {
        return ImmutablePythonClassName.builder()
                .packageName(packageName)
                .className(className)
                .build();
    }

    @Override
    default int compareTo(PythonClassName other) {
        int comparePackageName = packageName().compareTo(other.packageName());
        if (comparePackageName != 0) {
            return comparePackageName;
        }
        return className().compareTo(other.className());
    }

}
