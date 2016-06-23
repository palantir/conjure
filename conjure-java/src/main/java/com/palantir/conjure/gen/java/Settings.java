/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java;

import com.palantir.conjure.gen.java.TypeMapper.OptionalTypeStrategy;
import com.palantir.conjure.gen.java.types.BeanJavaTypeGenerator;
import com.palantir.conjure.gen.java.types.ImmutablesJavaTypeGenerator;
import org.immutables.value.Value;

@Value.Immutable
public interface Settings {

    enum TypeGeneratorImpl {
        Beans(new BeanJavaTypeGenerator()),
        Immutables(new ImmutablesJavaTypeGenerator());

        private final TypeGenerator generator;

        TypeGeneratorImpl(TypeGenerator generator) {
            this.generator = generator;
        }

        public TypeGenerator get() {
            return generator;
        }
    }

    @Value.Default
    default OptionalTypeStrategy optionalTypeStrategy() {
        return OptionalTypeStrategy.Java8;
    }

    @Value.Default
    default boolean ignoreUnknownProperties() {
        return false;
    }

    @Value.Default
    default TypeGeneratorImpl typeGenerator() {
        return TypeGeneratorImpl.Beans;
    }

    static Builder builder() {
        return new Builder();
    }

    static Settings standard() {
        return new Builder().build();
    }

    class Builder extends ImmutableSettings.Builder {}

}
