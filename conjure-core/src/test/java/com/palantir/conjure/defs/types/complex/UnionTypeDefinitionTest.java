/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.complex;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

public final class UnionTypeDefinitionTest {

    private static final UnionMemberTypeDefinition MEMBER = mock(UnionMemberTypeDefinition.class);

    @Test
    public void testUnionMemberKeyMustNotBeEmpty() throws Exception {
        assertThatThrownBy(() ->
                UnionTypeDefinition.builder()
                        .putUnion("", MEMBER)
                        .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Union member key must not be empty");
    }

    @Test
    public void testUnionMemberKeyMustNotBeIllegalJavaIdentifier() throws Exception {
        ImmutableList.of("%foo", "foo@").forEach(key -> assertThatThrownBy(() ->
                UnionTypeDefinition.builder()
                        .putUnion(key, MEMBER)
                        .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Union member key must be a valid Java identifier: %s", key));
    }
}
