/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.complex;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.palantir.conjure.defs.types.names.ConjurePackage;
import com.palantir.conjure.defs.types.names.FieldName;
import com.palantir.conjure.defs.types.names.TypeName;
import org.junit.Test;

public final class ObjectTypeDefinitionTest {

    @Test
    public void testUniqueFieldNamesValidator() {
        FieldDefinition mockField1 = mock(FieldDefinition.class);
        FieldDefinition mockField2 = mock(FieldDefinition.class);
        TypeName name = TypeName.of("Foo", ConjurePackage.of("package"));
        when(mockField1.fieldName()).thenReturn(FieldName.of("fooBar")).thenReturn(FieldName.of("foo-bar"));
        when(mockField2.fieldName()).thenReturn(FieldName.of("foo-bar")).thenReturn(FieldName.of("foo_bar"));

        ObjectTypeDefinition.Builder definition = ObjectTypeDefinition.builder()
                .typeName(name)
                .addFields(mockField1, mockField2);

        assertThatThrownBy(definition::build)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ObjectTypeDefinition must not contain duplicate field names (modulo case normalization): "
                        + "foo-bar vs fooBar");

        assertThatThrownBy(definition::build)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ObjectTypeDefinition must not contain duplicate field names (modulo case normalization): "
                        + "foo_bar vs foo-bar");
    }
}
