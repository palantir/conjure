/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.python.types;

import com.palantir.conjure.defs.ConjureImports;
import com.palantir.conjure.defs.TypesDefinition;
import com.palantir.conjure.defs.types.BaseObjectTypeDefinition;
import com.palantir.conjure.gen.python.PackageNameProcessor;
import com.palantir.conjure.gen.python.poet.PythonClass;

public interface BeanGenerator {

    PythonClass generateObject(TypesDefinition types,
            ConjureImports importedTypes,
            PackageNameProcessor packageNameProvider,
            String typeName,
            BaseObjectTypeDefinition typeDef);

}
