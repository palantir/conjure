/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.python.poet;

import java.util.Set;

public interface PythonClass extends Emittable {

    Set<PythonImport> requiredImports();

    String packageName();

}
