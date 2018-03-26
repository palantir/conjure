/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.complex;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.palantir.conjure.spec.ErrorCode;
import com.palantir.conjure.spec.ErrorDefinition;
import com.palantir.conjure.spec.ErrorNamespace;
import com.palantir.conjure.spec.FieldDefinition;
import com.palantir.conjure.spec.FieldName;
import com.palantir.conjure.spec.PrimitiveType;
import com.palantir.conjure.spec.Type;
import com.palantir.conjure.spec.TypeName;
import org.junit.Test;

public class ErrorTypeDefinitionValidatorTest {

    @Test
    public void testUniqueArgNamesValidator() {
        FieldDefinition safeArg1 = FieldDefinition.builder()
                .fieldName(FieldName.of("fooBar"))
                .type(Type.primitive(PrimitiveType.STRING))
                .build();
        FieldDefinition unsafeArg1 = FieldDefinition.builder()
                .fieldName(FieldName.of("foo-bar"))
                .type(Type.primitive(PrimitiveType.STRING))
                .build();
        ErrorDefinition definition1 = ErrorDefinition.builder()
                .errorName(TypeName.of("Foo", "package"))
                .namespace(ErrorNamespace.of("Test"))
                .code(ErrorCode.INVALID_ARGUMENT)
                .safeArgs(safeArg1)
                .unsafeArgs(unsafeArg1)
                .build();

        assertThatThrownBy(() -> ErrorTypeDefinitionValidator.validateAll(definition1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ErrorDefinition must not contain duplicate field names (modulo case normalization): "
                        + "foo-bar vs fooBar");

        FieldDefinition safeArg2 = FieldDefinition.builder()
                .fieldName(FieldName.of("foo-bar"))
                .type(Type.primitive(PrimitiveType.STRING))
                .build();
        FieldDefinition unsafeArg2 = FieldDefinition.builder()
                .fieldName(FieldName.of("foo_bar"))
                .type(Type.primitive(PrimitiveType.STRING))
                .build();
        ErrorDefinition definition2 = ErrorDefinition.builder()
                .errorName(TypeName.of("Foo", "package"))
                .namespace(ErrorNamespace.of("Test"))
                .code(ErrorCode.INVALID_ARGUMENT)
                .safeArgs(safeArg2)
                .unsafeArgs(unsafeArg2)
                .build();

        assertThatThrownBy(() -> ErrorTypeDefinitionValidator.validateAll(definition2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ErrorDefinition must not contain duplicate field names (modulo case normalization): "
                        + "foo-bar vs foo_bar");
    }

}
