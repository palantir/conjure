/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.complex;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.palantir.conjure.defs.types.names.ConjurePackage;
import com.palantir.conjure.defs.types.names.TypeName;
import org.junit.Test;

public final class EnumTypeDefinitionTest {

    @Test
    public void testUniqueEnumValues() {
        EnumTypeDefinition.Builder definition = EnumTypeDefinition.builder()
                .typeName(TypeName.of("Foo", ConjurePackage.of("package")))
                .addValues(EnumValueDefinition.builder().value("FOO").build())
                .addValues(EnumValueDefinition.builder().value("FOO").build());

        assertThatThrownBy(definition::build)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot declare a EnumTypeDefinition with duplicate enum values: FOO");
    }
}
