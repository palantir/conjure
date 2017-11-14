/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.poet;

import com.google.common.collect.Sets;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@Value.Immutable
public interface TypescriptInterface extends Emittable, TypescriptType {

    @Value.Default
    default boolean export() {
        return true;
    }

    /**
     * Omit this to make an inline interface, e.g. `let myPoint: {x: number; y: number} = `
     * https://basarat.gitbooks.io/typescript/docs/types/interfaces.html
     */
    Optional<String> name();

    List<TypescriptSimpleType> genericTypes();


    @Value.Default
    default SortedSet<TypescriptFieldSignature> propertySignatures() {
        return Sets.newTreeSet();
    }

    @Value.Default
    default SortedSet<TypescriptInterfaceFunctionSignature> methodSignatures() {
        return Sets.newTreeSet();
    }

    @Override
    default void emit(TypescriptPoetWriter writer) {
        if (export()) {
            writer.writeIndented("export ");
        }

        if (name().isPresent()) {
            writer.write("interface " + name().get());
            if (genericTypes().size() > 0) {
                writer.write("<");
                writer.emitJoin(genericTypes(), ", ");
                writer.write(">");
            }
            writer.write(" ");
        }

        writer.writeLine("{")
                .increaseIndent();

        propertySignatures().forEach(property -> {
            writer.writeIndented();
            property.emit(writer);
            writer.writeLine(";");
        });
        if (!propertySignatures().isEmpty() && !methodSignatures().isEmpty()) {
            writer.writeLine();
        }

        if (!methodSignatures().isEmpty()) {
            TypescriptInterfaceFunctionSignature finalSignature = methodSignatures().last();
            methodSignatures().forEach(method -> {
                writer.writeIndented();
                method.emit(writer);
                writer.writeLine(";");
                if (!method.equals(finalSignature)) {
                    writer.writeLine();
                }
            });
        }
        writer.decreaseIndent().writeIndentedLine("}");
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableTypescriptInterface.Builder {}
}
