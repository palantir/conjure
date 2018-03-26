/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.python.poet;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.types.names.ConjurePackageWrapper;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface PythonClassName extends Comparable<PythonClassName> {

    String conjurePackage();

    String className();

    static PythonClassName of(String conjurePackage, String className) {
        ConjurePackageWrapper.validate(conjurePackage);
        return ImmutablePythonClassName.builder()
                .conjurePackage(conjurePackage)
                .className(className)
                .build();
    }

    @Override
    default int compareTo(PythonClassName other) {
        int comparePackageName = conjurePackage().compareTo(other.conjurePackage());
        if (comparePackageName != 0) {
            return comparePackageName;
        }
        return className().compareTo(other.className());
    }

}
