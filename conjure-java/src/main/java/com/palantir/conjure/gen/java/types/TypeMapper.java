/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import com.palantir.conjure.defs.ConjureImports;
import com.palantir.conjure.defs.TypesDefinition;
import com.palantir.conjure.defs.types.ConjureType;
import com.palantir.conjure.gen.java.types.ClassNameVisitor.Factory;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

public final class TypeMapper {

    public enum OptionalTypeStrategy {
        JAVA8(java.util.Optional.class, "empty"),
        GUAVA(com.google.common.base.Optional.class, "absent");

        private final ClassName clazz;
        private final String absentMethodName;

        OptionalTypeStrategy(Class<?> clazz, String absentMethodName) {
            this.clazz = ClassName.get(clazz);
            this.absentMethodName = absentMethodName;
        }

        public ClassName getClassName() {
            return clazz;
        }

        public String getAbsentMethodName() {
            return absentMethodName;
        }
    }

    private final TypesDefinition types;
    private final ConjureImports importedTypes;
    private final OptionalTypeStrategy optionalTypeStrategy;
    private final Factory classNameVisitorFactory;

    public TypeMapper(TypesDefinition types, ConjureImports importedTypes,
            OptionalTypeStrategy optionalTypeStrategy) {
        this(types, importedTypes, optionalTypeStrategy, DefaultClassNameVisitor::new);
    }

    public TypeMapper(TypesDefinition types, ConjureImports importedTypes,
            OptionalTypeStrategy optionalTypeStrategy, Factory classNameVisitorFactory) {
        this.types = types;
        this.importedTypes = importedTypes;
        this.optionalTypeStrategy = optionalTypeStrategy;
        this.classNameVisitorFactory = classNameVisitorFactory;
    }

    public TypeName getClassName(ConjureType type) {
        return type.visit(classNameVisitorFactory.create(types, importedTypes, optionalTypeStrategy));
    }

    public ClassName getOptionalType() {
        return optionalTypeStrategy.getClassName();
    }

    public String getAbsentMethodName() {
        return optionalTypeStrategy.getAbsentMethodName();
    }

}
