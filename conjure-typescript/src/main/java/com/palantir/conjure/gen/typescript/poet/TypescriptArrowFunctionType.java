/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.poet;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import java.util.List;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@Value.Immutable
public interface TypescriptArrowFunctionType extends TypescriptType {
    List<TypescriptSimpleType> genericTypes();
    List<TypescriptTypeSignature> parameters();
    TypescriptType returnType();

    @Override
    default void emit(TypescriptPoetWriter writer) {
        if (genericTypes().size() > 0) {
            writer.write("<");
            writer.emitJoin(genericTypes(), ", ");
            writer.write(">");
        }
        writer.write("(");
        writer.emitJoin(parameters(), ", ");
        writer.write(") => ");
        writer.emit(returnType());
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableTypescriptArrowFunctionType.Builder {}
}
