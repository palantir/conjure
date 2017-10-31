/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.python.poet;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.types.names.ConjurePackage;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface PythonEnum extends PythonClass {

    ImmutableSet<PythonImport> DEFAULT_IMPORTS = ImmutableSet.of(
            PythonImport.of(PythonClassName.of(ConjurePackage.of("conjure"), "*"), Optional.empty()));

    @Override
    default Set<PythonImport> requiredImports() {
        return DEFAULT_IMPORTS;
    }

    String className();

    Optional<String> docs();

    List<PythonEnumValue> values();

    @Override
    default void emit(PythonPoetWriter poetWriter) {
        poetWriter.maintainingIndent(() -> {
            poetWriter.writeIndentedLine(String.format("class %s(ConjureEnumType):", className()));
            poetWriter.increaseIndent();
            docs().ifPresent(docs -> poetWriter.writeIndentedLine(String.format("'''%s'''", docs)));

            poetWriter.writeLine();

            List<PythonEnumValue> allValues = ImmutableList.<PythonEnumValue>builder()
                    .addAll(values())
                    .add(PythonEnumValue.of("UNKNOWN", Optional.empty()))
                    .build();

            allValues.forEach(value -> {
                poetWriter.writeIndentedLine("%s = '%s'", value.name(), value.name());
                poetWriter.writeIndentedLine("'''%s'''", value.name());
            });

            poetWriter.decreaseIndent();
            poetWriter.writeLine();
        });
    }

    class Builder extends ImmutablePythonEnum.Builder {}

    static Builder builder() {
        return new Builder();
    }

    @Value.Immutable
    @ConjureImmutablesStyle
    public interface PythonEnumValue {

        String name();

        Optional<String> docs();

        static PythonEnumValue of(String name, Optional<String> docs) {
            return ImmutablePythonEnumValue.builder().name(name).docs(docs).build();
        }

    }
}
