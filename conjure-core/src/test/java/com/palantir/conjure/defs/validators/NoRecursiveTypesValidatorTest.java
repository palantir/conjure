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
import com.palantir.conjure.defs.types.FieldName;
import com.palantir.conjure.defs.types.ObjectTypeDefinition;
import com.palantir.conjure.defs.types.TypeName;
import com.palantir.parsec.ParseException;
import org.junit.Test;

public final class NoRecursiveTypesValidatorTest {

    @Test
    public void testNoSelfRecursiveType() throws Exception {
        ObjectsDefinition imports = mock(ObjectsDefinition.class);
        when(imports.objects()).thenReturn(
                ImmutableMap.of(
                        TypeName.of("Foo"),
                        ObjectTypeDefinition.builder().putFields(FieldName.of("self"), FieldDefinition.of(
                                ConjureType.fromString("Foo"))).build()
                )
        );

        assertThatThrownBy(() -> ObjectsDefinitionValidator.NO_RECURSIVE_TYPES.validate(imports))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Illegal recursive data type: Foo -> Foo");
    }

    @Test
    public void testRecursiveTypeOkInReference() throws Exception {
        ObjectsDefinition imports = mock(ObjectsDefinition.class);
        when(imports.objects()).thenReturn(
                ImmutableMap.of(
                        TypeName.of("Foo"),
                        ObjectTypeDefinition.builder().putAllFields(ImmutableMap.of(
                                FieldName.of("selfOptional"), field("optional<Foo>"),
                                FieldName.of("selfMap"), field("map<string, Foo>"),
                                FieldName.of("selfSet"), field("set<Foo>"),
                                FieldName.of("selfList"), field("list<Foo>")
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
                        TypeName.of("Foo"),
                        ObjectTypeDefinition.builder().putFields(FieldName.of("bar"), field("Bar")).build(),
                        TypeName.of("Bar"),
                        ObjectTypeDefinition.builder().putFields(FieldName.of("foo"), field("Foo")).build()
                )
        );

        assertThatThrownBy(() -> ObjectsDefinitionValidator.NO_RECURSIVE_TYPES.validate(imports))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Illegal recursive data type: Bar -> Foo -> Bar");
    }

    private static FieldDefinition field(String type) throws ParseException {
        return FieldDefinition.of(ConjureType.fromString(type));
    }

}
