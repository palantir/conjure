/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.validators;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import com.palantir.conjure.defs.ObjectsDefinition;
import com.palantir.conjure.defs.types.ConjureType;
import com.palantir.conjure.defs.types.FieldDefinition;
import com.palantir.conjure.defs.types.ObjectTypeDefinition;
import com.palantir.parsec.ParseException;
import org.junit.Test;

public final class NoRecursiveTypesValidatorTest {

    @Test
    public void testNoSelfRecursiveType() throws Exception {
        ObjectsDefinition imports = mock(ObjectsDefinition.class);
        when(imports.objects()).thenReturn(
                ImmutableMap.of(
                        "foo",
                        ObjectTypeDefinition.builder().putFields("self", FieldDefinition.of(
                                ConjureType.fromString("foo"))).build()
                )
        );

        assertThatThrownBy(() -> ObjectsDefinitionValidator.NO_RECURSIVE_TYPES.validate(imports))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Illegal recursive data type: foo -> foo");
    }

    @Test
    public void testRecursiveTypeOkInReference() throws Exception {
        ObjectsDefinition imports = mock(ObjectsDefinition.class);
        when(imports.objects()).thenReturn(
                ImmutableMap.of(
                        "foo",
                        ObjectTypeDefinition.builder().putAllFields(ImmutableMap.of(
                                "selfOptional", field("optional<foo>"),
                                "selfMap", field("map<string, foo>"),
                                "selfSet", field("set<foo>"),
                                "selfList", field("list<foo>")
                        )).build()
                )
        );

        ObjectsDefinitionValidator.NO_RECURSIVE_TYPES.validate(imports);
    }

    @Test
    public void testNoRecursiveCycleType() throws Exception {
        ObjectsDefinition imports = mock(ObjectsDefinition.class);
        when(imports.objects()).thenReturn(
                ImmutableMap.of(
                        "foo", ObjectTypeDefinition.builder().putFields("bar", field("bar")).build(),
                        "bar", ObjectTypeDefinition.builder().putFields("foo", field("foo")).build()
                )
        );

        assertThatThrownBy(() -> ObjectsDefinitionValidator.NO_RECURSIVE_TYPES.validate(imports))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Illegal recursive data type: bar -> foo -> bar");
    }

    private static FieldDefinition field(String type) throws ParseException {
        return FieldDefinition.of(ConjureType.fromString(type));
    }

}
