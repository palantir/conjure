/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.poet;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@Value.Immutable
public interface TypescriptTypeAlias extends TypescriptStatement {
    String name();
    TypescriptType type();

    @Override
    default void emit(TypescriptPoetWriter writer) {
        writer.writeIndented(String.format("export type %s", name()));
        writer.write(" = ");
        writer.emit(type());
        writer.writeLine(";");
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableTypescriptTypeAlias.Builder {}
}
