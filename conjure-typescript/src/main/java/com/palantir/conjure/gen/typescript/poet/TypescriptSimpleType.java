/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.poet;

import com.google.common.collect.ImmutableList;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@Value.Immutable
public interface TypescriptSimpleType extends TypescriptType {

    @Value.Parameter
    String name();

    @Value.Derived
    default boolean isPrimitive() {
        return ImmutableList.of("number", "string", "boolean").contains(name());
    }

    @Override
    default void emit(TypescriptPoetWriter writer) {
        writer.write(name());
    }

    static TypescriptSimpleType of(String name) {
        return ImmutableTypescriptSimpleType.of(name);
    }
}
