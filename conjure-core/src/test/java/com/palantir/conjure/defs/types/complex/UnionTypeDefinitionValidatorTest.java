/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.complex;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableList;
import com.palantir.conjure.spec.FieldDefinition;
import com.palantir.conjure.spec.FieldName;
import com.palantir.conjure.spec.PrimitiveType;
import com.palantir.conjure.spec.Type;
import com.palantir.conjure.spec.TypeName;
import com.palantir.conjure.spec.UnionDefinition;
import org.junit.Test;

public final class UnionTypeDefinitionValidatorTest {

    @Test
    public void testUnionMemberKeyMustNotBeEmpty() {
        FieldDefinition fieldDefinition = FieldDefinition.builder()
                .fieldName(FieldName.of(""))
                .type(Type.primitive(PrimitiveType.STRING))
                .build();

        assertThatThrownBy(() ->
                UnionTypeDefinitionValidator.validateAll(UnionDefinition.builder()
                        .union(fieldDefinition)
                        .typeName(TypeName.of("string", ""))
                        .build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith("Union member key must not be empty");
    }

    @Test
    public void testUnionMemberKeyMustNotBeIllegalJavaIdentifier() {
        ImmutableList.of("%foo", "foo@").forEach(key -> {
            FieldDefinition fieldDefinition = FieldDefinition.builder()
                    .fieldName(FieldName.of(key))
                    .type(Type.primitive(PrimitiveType.STRING))
                    .build();

            assertThatThrownBy(() ->
                    UnionTypeDefinitionValidator.validateAll(UnionDefinition.builder()
                            .union(fieldDefinition)
                            .typeName(TypeName.of("string", ""))
                            .build()))
                    .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageStartingWith(
                                String.format("Union member key must be a valid Java identifier: %s", key));
        });
    }

    @Test
    public void testUnionMemberKeyMustNotHaveTrailingUnderscore() {
        FieldDefinition fieldDefinition = FieldDefinition.builder()
                .fieldName(FieldName.of("foo_"))
                .type(Type.primitive(PrimitiveType.STRING))
                .build();
        assertThatThrownBy(() ->
                UnionTypeDefinitionValidator.validateAll(UnionDefinition.builder()
                        .union(fieldDefinition)
                        .typeName(TypeName.of("string", ""))
                        .build()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageStartingWith("Union member key must not end with an underscore: foo_");
    }
}
