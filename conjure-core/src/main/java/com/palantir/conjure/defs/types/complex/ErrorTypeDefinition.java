/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.complex;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.types.BaseObjectTypeDefinition;
import com.palantir.conjure.defs.types.ConjureTypeParserVisitor;
import com.palantir.conjure.defs.types.ObjectTypeDefParserVisitor;
import com.palantir.conjure.defs.types.names.ErrorCode;
import com.palantir.conjure.defs.types.names.ErrorNamespace;
import com.palantir.conjure.defs.types.names.FieldName;
import com.palantir.conjure.defs.types.names.TypeName;
import java.util.Map;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface ErrorTypeDefinition extends BaseObjectTypeDefinition {

    ErrorNamespace namespace();

    ErrorCode code();

    Map<FieldName, FieldDefinition> safeArgs();

    Map<FieldName, FieldDefinition> unsafeArgs();

    @Value.Check
    default void check() {
        for (ErrorTypeDefinitionValidator validator : ErrorTypeDefinitionValidator.values()) {
            validator.validate(this);
        }
    }

    static ErrorTypeDefinition parseFrom(
            TypeName name,
            com.palantir.conjure.parser.types.complex.ErrorTypeDefinition def,
            ConjureTypeParserVisitor.TypeNameResolver typeResolver) {
        return builder()
                .typeName(name)
                .namespace(ErrorNamespace.of(def.namespace().name()))
                .code(ErrorCode.of(def.code().name()))
                .safeArgs(ObjectTypeDefParserVisitor.parseFieldDef(def.safeArgs(), typeResolver))
                .unsafeArgs(ObjectTypeDefParserVisitor.parseFieldDef(def.unsafeArgs(), typeResolver))
                .docs(def.docs())
                .build();
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableErrorTypeDefinition.Builder {}

}
