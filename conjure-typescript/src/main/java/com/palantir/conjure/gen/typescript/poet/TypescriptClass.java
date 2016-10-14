/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.poet;

import com.google.common.base.Optional;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import java.util.List;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@Value.Immutable
public interface TypescriptClass extends Emittable {
    List<AssignStatement> fields();
    Optional<TypescriptConstructor> constructor();
    List<TypescriptFunction> methods();
    String name();

    @Override
    default void emit(TypescriptPoetWriter writer) {
        writer.writeIndentedLine("export class " + name() + " {");
        writer.increaseIndent();
        if (fields().size() > 0) {
            fields().forEach(writer::emit);
            writer.writeLine();
        }
        if (constructor().isPresent()) {
            writer.emit(constructor().get());
            writer.writeLine();
        }
        methods().forEach(method -> {
            writer.emit(method);
            if (!method.equals(methods().get(methods().size() - 1))) {
                writer.writeLine();
            }
        });
        writer.decreaseIndent();
        writer.writeIndentedLine("}");
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableTypescriptClass.Builder {}
}
