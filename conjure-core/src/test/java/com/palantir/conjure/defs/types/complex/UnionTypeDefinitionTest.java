/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.complex;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.palantir.conjure.defs.types.names.ConjurePackage;
import com.palantir.conjure.defs.types.names.TypeName;
import org.junit.Test;

public final class UnionTypeDefinitionTest {

    private final FieldDefinition mockFieldDef = mock(FieldDefinition.class, RETURNS_DEEP_STUBS);

    @Test
    public void testUnionMemberKeyMustNotBeEmpty() {
        when(mockFieldDef.fieldName().name()).thenReturn("");
        assertThatThrownBy(() ->
                UnionTypeDefinition.builder()
                        .addUnion(mockFieldDef)
                        .typeName(TypeName.of("string", ConjurePackage.PRIMITIVE))
                        .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith("Union member key must not be empty");
    }

    @Test
    public void testUnionMemberKeyMustNotBeIllegalJavaIdentifier() {
        ImmutableList.of("%foo", "foo@").forEach(key -> {
            when(mockFieldDef.fieldName().name()).thenReturn(key);
            assertThatThrownBy(() -> UnionTypeDefinition.builder()
                    .addUnion(mockFieldDef)
                    .typeName(TypeName.of("string", ConjurePackage.PRIMITIVE))
                    .build())
                    .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageStartingWith(
                                String.format("Union member key must be a valid Java identifier: %s", key));
        });
    }

    @Test
    public void testUnionMemberKeyMustNotHaveTrailingUnderscore() {
        when(mockFieldDef.fieldName().name()).thenReturn("foo_");
        assertThatThrownBy(() -> UnionTypeDefinition.builder()
                .addUnion(mockFieldDef)
                .typeName(TypeName.of("string", ConjurePackage.PRIMITIVE))
                .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageStartingWith("Union member key must not end with an underscore: foo_");
    }
}
