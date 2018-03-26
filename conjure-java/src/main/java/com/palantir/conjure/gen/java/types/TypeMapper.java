/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import com.palantir.conjure.gen.java.types.ClassNameVisitor.Factory;
import com.palantir.conjure.spec.Type;
import com.palantir.conjure.spec.TypeDefinition;
import com.squareup.javapoet.TypeName;
import java.util.List;

public final class TypeMapper {

    private final List<TypeDefinition> types;
    private final Factory classNameVisitorFactory;

    public TypeMapper(List<TypeDefinition> types) {
        this(types, DefaultClassNameVisitor::new);
    }

    public TypeMapper(List<TypeDefinition> types,
            Factory classNameVisitorFactory) {
        this.types = types;
        this.classNameVisitorFactory = classNameVisitorFactory;
    }

    public TypeName getClassName(Type type) {
        return type.accept(classNameVisitorFactory.create(types));
    }
}
