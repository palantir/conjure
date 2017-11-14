/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.poet;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@Value.Immutable
public interface TypescriptFunctionSignature extends Comparable<TypescriptFunctionSignature>, Emittable {

    String name();
    List<TypescriptSimpleType> genericTypes();
    List<TypescriptTypeSignature> parameters();
    Optional<TypescriptType> returnType();

    @Override
    default int compareTo(TypescriptFunctionSignature other) {
        return this.name().compareTo(other.name());
    }

    @Override
    default void emit(TypescriptPoetWriter writer) {
        writer.write(name());
        if (genericTypes().size() > 0) {
            writer.write("<");
            writer.emitJoin(genericTypes(), ", ");
            writer.write(">");
        }
        writer.write("(");

        List<TypescriptTypeSignature> parametersOptionalAtEnd = parameters().stream()
                // this sort is stable
                .sorted(Comparator.comparing(TypescriptTypeSignature::isOptional))
                .collect(Collectors.toList());

        if (parametersOptionalAtEnd.size() > 0) {
            writer.writeLine();
            writer.increaseIndent();
            writer.writeIndented();
            parametersOptionalAtEnd.get(0).emit(writer);
            parametersOptionalAtEnd.subList(1, parametersOptionalAtEnd.size()).forEach(parameter -> {
                writer.writeLine(",");
                writer.writeIndented();
                parameter.emit(writer);
            });
            writer.writeLine();
            writer.decreaseIndent();
            writer.writeIndented();
        }
        writer.write(")");
        if (returnType().isPresent()) {
            writer.write(": ");
            returnType().get().emit(writer);
        }
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableTypescriptFunctionSignature.Builder {}
}
