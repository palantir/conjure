/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.poet;

import com.google.common.base.Preconditions;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import java.util.List;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@Value.Immutable
public interface TypescriptUnionType extends TypescriptType {
    List<TypescriptType> types();

    @Value.Check
    default void check() {
        Preconditions.checkArgument(!types().isEmpty(), "'types' must contain at least one TypescriptType");
    }

    @Override
    default void emit(TypescriptPoetWriter writer) {
        writer.write("(");
        types().get(0).emit(writer);
        if (types().size() > 1) {
            types().subList(1, types().size()).forEach(type -> {
                writer.write(" | ");
                type.emit(writer);
            });
        }
        writer.write(")");
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableTypescriptUnionType.Builder {}
}
