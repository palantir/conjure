/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import com.palantir.conjure.defs.types.complex.FieldDefinition;
import com.palantir.conjure.defs.types.complex.ObjectTypeDefinition;
import com.palantir.conjure.defs.types.names.FieldName;
import com.palantir.conjure.defs.types.names.TypeName;
import com.palantir.parsec.ParseException;
import org.junit.Test;

public final class ObjectsDefinitionTest {

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
                .hasMessageStartingWith("Illegal recursive data type: ");
    }

    private static FieldDefinition field(String type) throws ParseException {
        return FieldDefinition.of(ConjureType.fromString(type));
    }

}
