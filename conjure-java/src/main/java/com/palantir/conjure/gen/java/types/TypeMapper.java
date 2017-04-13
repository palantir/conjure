/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import com.palantir.conjure.defs.types.ConjureType;
import com.palantir.conjure.defs.types.TypesDefinition;
import com.palantir.conjure.defs.types.reference.ConjureImports;
import com.palantir.conjure.gen.java.types.ClassNameVisitor.Factory;
import com.squareup.javapoet.TypeName;

public final class TypeMapper {

    private final TypesDefinition types;
    private final ConjureImports importedTypes;
    private final Factory classNameVisitorFactory;

    public TypeMapper(TypesDefinition types, ConjureImports importedTypes) {
        this(types, importedTypes, DefaultClassNameVisitor::new);
    }

    public TypeMapper(TypesDefinition types, ConjureImports importedTypes,
            Factory classNameVisitorFactory) {
        this.types = types;
        this.importedTypes = importedTypes;
        this.classNameVisitorFactory = classNameVisitorFactory;
    }

    public TypeName getClassName(ConjureType type) {
        return type.visit(classNameVisitorFactory.create(types, importedTypes));
    }
}
