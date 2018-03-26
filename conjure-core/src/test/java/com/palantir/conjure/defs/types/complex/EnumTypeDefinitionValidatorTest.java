/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.complex;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.palantir.conjure.spec.EnumDefinition;
import com.palantir.conjure.spec.EnumValueDefinition;
import com.palantir.conjure.spec.TypeName;
import org.junit.Test;

public final class EnumTypeDefinitionValidatorTest {

    @Test
    public void testUniqueEnumValues() {
        EnumDefinition.Builder definition = EnumDefinition.builder()
                .typeName(TypeName.of("Foo", "package"))
                .values(EnumValueDefinition.builder().value("FOO").build())
                .values(EnumValueDefinition.builder().value("FOO").build());

        assertThatThrownBy(() -> EnumTypeDefinitionValidator.validateAll(definition.build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot declare a EnumTypeDefinition with duplicate enum values: FOO");
    }
}
