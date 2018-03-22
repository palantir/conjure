/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.complex;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.palantir.conjure.defs.types.names.ConjurePackage;
import com.palantir.conjure.defs.types.names.ErrorCode;
import com.palantir.conjure.defs.types.names.ErrorNamespace;
import com.palantir.conjure.defs.types.names.FieldName;
import com.palantir.conjure.defs.types.names.TypeName;
import org.junit.Test;

public class ErrorTypeDefinitionTest {

    @Test
    public void testUniqueArgNamesValidator() {
        FieldDefinition mockField = mock(FieldDefinition.class);
        when(mockField.fieldName())
                .thenReturn(FieldName.of("fooBar"))
                .thenReturn(FieldName.of("foo-bar"))
                .thenReturn(FieldName.of("foo-bar"))
                .thenReturn(FieldName.of("foo_bar"));

        ErrorTypeDefinition.Builder definition = ErrorTypeDefinition.builder()
                .errorName(TypeName.of("Foo", ConjurePackage.of("package")))
                .namespace(ErrorNamespace.of("Test"))
                .code(ErrorCode.of("INVALID_ARGUMENT"))
                .addSafeArgs(mockField)
                .addUnsafeArgs(mockField);

        assertThatThrownBy(definition::build)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ErrorTypeDefinition must not contain duplicate field names (modulo case normalization): "
                        + "foo-bar vs fooBar");

        definition = ErrorTypeDefinition.builder()
                .errorName(TypeName.of("Foo", ConjurePackage.of("package")))
                .namespace(ErrorNamespace.of("Test"))
                .code(ErrorCode.of("INVALID_ARGUMENT"))
                .addSafeArgs(mockField)
                .addUnsafeArgs(mockField);

        assertThatThrownBy(definition::build)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ErrorTypeDefinition must not contain duplicate field names (modulo case normalization): "
                        + "foo_bar vs foo-bar");
    }

}
