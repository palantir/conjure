/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.poet;

import com.google.common.collect.Sets;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import java.util.SortedSet;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@Value.Immutable
public interface TypescriptInterface extends Emittable {

    String name();

    @Value.Default
    default SortedSet<TypescriptTypeSignature> propertySignatures() {
        return Sets.newTreeSet();
    }

    @Value.Default
    default SortedSet<TypescriptFunctionSignature> methodSignatures() {
        return Sets.newTreeSet();
    }

    @Override
    default void emit(TypescriptPoetWriter writer) {
        writer.writeIndentedLine("export interface "  + name() +  " {")
                .increaseIndent();
        propertySignatures().forEach(property -> {
            writer.writeIndented();
            property.emit(writer);
            writer.writeLine(";");
        });
        if (!propertySignatures().isEmpty() && !methodSignatures().isEmpty()) {
            writer.writeLine();
        }
        methodSignatures().forEach(method -> {
            writer.writeIndented();
            method.emit(writer);
            writer.writeLine(";");
        });
        writer.decreaseIndent().writeIndentedLine("}");
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableTypescriptInterface.Builder {}
}
