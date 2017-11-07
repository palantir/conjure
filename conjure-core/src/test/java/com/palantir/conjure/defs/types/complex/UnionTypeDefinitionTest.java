/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.complex;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableList;
import com.palantir.conjure.defs.types.names.FieldName;
import org.junit.Test;

public final class UnionTypeDefinitionTest {

    private static final UnionMemberTypeDefinition MEMBER = mock(UnionMemberTypeDefinition.class);

    @Test
    public void testUnionMemberKeyMustNotBeEmpty() throws Exception {
        assertThatThrownBy(() ->
                UnionTypeDefinition.builder()
                        .putUnion(FieldName.of(""), MEMBER)
                        .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith("FieldName \"\" must follow one of the following patterns");
    }

    @Test
    public void testUnionMemberKeyMustNotBeIllegalJavaIdentifier() throws Exception {
        ImmutableList.of("%foo", "foo@").forEach(key -> assertThatThrownBy(() ->
                UnionTypeDefinition.builder()
                        .putUnion(FieldName.of(key), MEMBER)
                        .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith(
                        String.format("FieldName \"%s\" must follow one of the following patterns", key)));
    }

    @Test
    public void testUnionMemberKeyMustNotHaveTrailingUnderscore() throws Exception {
        assertThatThrownBy(() -> UnionTypeDefinition.builder().putUnion(FieldName.of("foo_"), MEMBER).build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageStartingWith("FieldName \"foo_\" must follow one of the following patterns");
    }
}
