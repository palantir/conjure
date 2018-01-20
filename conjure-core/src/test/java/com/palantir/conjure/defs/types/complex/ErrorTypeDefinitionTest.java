/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.complex;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.palantir.conjure.defs.types.names.ConjurePackage;
import com.palantir.conjure.defs.types.names.ErrorCode;
import com.palantir.conjure.defs.types.names.ErrorNamespace;
import com.palantir.conjure.defs.types.names.FieldName;
import com.palantir.conjure.defs.types.names.TypeName;
import org.junit.Test;

public class ErrorTypeDefinitionTest {

    private static final FieldDefinition FIELD = mock(FieldDefinition.class);

    @Test
    public void testUniqueArgNamesValidator() throws Exception {
        ErrorTypeDefinition.Builder definition = ErrorTypeDefinition.builder()
                .typeName(TypeName.of("Foo", ConjurePackage.of("package")))
                .namespace(ErrorNamespace.of("Test"))
                .code(ErrorCode.of("INVALID_ARGUMENT"))
                .putSafeArgs(FieldName.of("fooBar"), FIELD)
                .putUnsafeArgs(FieldName.of("foo-bar"), FIELD);

        assertThatThrownBy(definition::build)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ErrorTypeDefinition must not contain duplicate field names (modulo case normalization): "
                        + "foo-bar vs fooBar");

        definition = ErrorTypeDefinition.builder()
                .typeName(TypeName.of("Foo", ConjurePackage.of("package")))
                .namespace(ErrorNamespace.of("Test"))
                .code(ErrorCode.of("INVALID_ARGUMENT"))
                .putSafeArgs(FieldName.of("foo-bar"), FIELD)
                .putUnsafeArgs(FieldName.of("foo_bar"), FIELD);

        assertThatThrownBy(definition::build)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ErrorTypeDefinition must not contain duplicate field names (modulo case normalization): "
                        + "foo_bar vs foo-bar");
    }

}
