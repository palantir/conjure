/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.poet;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@Value.Immutable
public interface TypescriptDocumentation extends Emittable {

    String docs();

    @Override
    default void emit(TypescriptPoetWriter writer) {
        writer.write("/** ");
        writer.write(docs());
        writer.write(" */");
        writer.writeLine();
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableTypescriptDocumentation.Builder {}

}
