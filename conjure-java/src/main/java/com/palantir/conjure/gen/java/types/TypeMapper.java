/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import com.palantir.conjure.defs.types.ConjureType;
import com.palantir.conjure.defs.types.TypesDefinition;
import com.palantir.conjure.gen.java.types.ClassNameVisitor.Factory;
import com.squareup.javapoet.TypeName;

public final class TypeMapper {

    private final TypesDefinition types;
    private final Factory classNameVisitorFactory;

    public TypeMapper(TypesDefinition types) {
        this(types, DefaultClassNameVisitor::new);
    }

    public TypeMapper(TypesDefinition types,
            Factory classNameVisitorFactory) {
        this.types = types;
        this.classNameVisitorFactory = classNameVisitorFactory;
    }

    public TypeName getClassName(ConjureType type) {
        return type.visit(classNameVisitorFactory.create(types));
    }
}
