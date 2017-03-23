/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.python.poet;

public interface Emittable {

    void emit(PythonPoetWriter poetWriter);

}
