/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.validator;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.palantir.conjure.spec.EnumDefinition;
import com.palantir.conjure.spec.EnumValueDefinition;
import com.palantir.conjure.spec.TypeName;
import org.junit.Test;

public final class EnumDefinitionValidatorTest {

    @Test
    public void testUniqueEnumValues() {
        EnumDefinition.Builder definition = EnumDefinition.builder()
                .typeName(TypeName.of("Foo", "package"))
                .values(EnumValueDefinition.builder().value("FOO").build())
                .values(EnumValueDefinition.builder().value("FOO").build());

        assertThatThrownBy(() -> EnumDefinitionValidator.validateAll(definition.build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot declare a EnumTypeDefinition with duplicate enum values: FOO");
    }
}
