/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.poet;

import com.google.common.base.Splitter;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@Value.Immutable
public interface TypescriptInterfaceFunctionSignature
        extends Comparable<TypescriptInterfaceFunctionSignature>, Emittable {

    Optional<String> docs();
    TypescriptFunctionSignature signature();

    @Override
    default int compareTo(TypescriptInterfaceFunctionSignature other) {
        return this.signature().compareTo(other.signature());
    }

    @Override
    default void emit(TypescriptPoetWriter writer) {
        if (docs().isPresent()) {
            List<String> docLines = Splitter.on("\n")
                    .splitToList(docs().get())
                    .stream()
                    .filter(line -> !line.isEmpty())
                    .collect(Collectors.toList());

            if (docLines.size() > 1) {
                writer.writeLine("/**");
                docLines.stream().forEach(line -> {
                    writer.writeIndented(" * ");
                    writer.writeLine(line);
                });
                writer.writeIndentedLine(" */");
                writer.writeIndented();
            } else {
                docLines.stream().findFirst().ifPresent(line -> {
                    writer.write("/** ");
                    writer.write(line);
                    writer.writeLine(" */");
                    writer.writeIndented();
                });
            }
        }

        signature().emit(writer);
    }

    static Builder builder() {
        return new Builder();
    }
    class Builder extends ImmutableTypescriptInterfaceFunctionSignature.Builder {}
}
