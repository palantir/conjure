/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.python.types;

import com.palantir.conjure.defs.types.BaseObjectTypeDefinition;
import com.palantir.conjure.defs.types.TypesDefinition;
import com.palantir.conjure.defs.types.names.TypeName;
import com.palantir.conjure.gen.python.PackageNameProcessor;
import com.palantir.conjure.gen.python.poet.PythonClass;

public interface BeanGenerator {

    PythonClass generateObject(
            TypesDefinition types,
            PackageNameProcessor packageNameProvider,
            TypeName typeName,
            BaseObjectTypeDefinition typeDef);

}
