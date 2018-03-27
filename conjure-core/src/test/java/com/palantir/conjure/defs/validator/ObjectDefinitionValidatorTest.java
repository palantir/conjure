/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.validator;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.palantir.conjure.spec.FieldDefinition;
import com.palantir.conjure.spec.FieldName;
import com.palantir.conjure.spec.ObjectDefinition;
import com.palantir.conjure.spec.PrimitiveType;
import com.palantir.conjure.spec.Type;
import com.palantir.conjure.spec.TypeName;
import org.junit.Test;

public final class ObjectDefinitionValidatorTest {

    @Test
    public void testUniqueFieldNamesValidator() {
        testUniqueFieldNameValidator("fooBar", "foo-bar");
        testUniqueFieldNameValidator("foo_bar", "foo-bar");
    }

    private void testUniqueFieldNameValidator(String fieldName1, String fieldName2) {
        FieldDefinition field1 = FieldDefinition.builder()
                .fieldName(FieldName.of(fieldName1))
                .type(Type.primitive(PrimitiveType.STRING))
                .build();
        FieldDefinition field2 = FieldDefinition.builder()
                .fieldName(FieldName.of(fieldName2))
                .type(Type.primitive(PrimitiveType.STRING))
                .build();
        TypeName name = TypeName.of("Foo", "package");
        ObjectDefinition definition = ObjectDefinition.builder()
                .typeName(name)
                .fields(field1)
                .fields(field2)
                .build();

        assertThatThrownBy(() -> ObjectDefinitionValidator.validate(definition))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(String.format("ObjectDefinition must not contain duplicate field names "
                        + "(modulo case normalization): %s vs %s", fieldName2, fieldName1));
    }
}
