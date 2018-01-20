/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.palantir.conjure.defs.types.collect.ListType;
import com.palantir.conjure.defs.types.collect.MapType;
import com.palantir.conjure.defs.types.collect.OptionalType;
import com.palantir.conjure.defs.types.collect.SetType;
import com.palantir.conjure.defs.types.complex.FieldDefinition;
import com.palantir.conjure.defs.types.complex.ObjectTypeDefinition;
import com.palantir.conjure.defs.types.names.ConjurePackage;
import com.palantir.conjure.defs.types.names.FieldName;
import com.palantir.conjure.defs.types.names.TypeName;
import com.palantir.conjure.defs.types.primitive.PrimitiveType;
import com.palantir.conjure.defs.types.reference.LocalReferenceType;
import com.palantir.parsec.ParseException;
import java.util.List;
import java.util.Optional;
import org.junit.Test;

public final class ObjectsDefinitionTest {

    private static final ConjurePackage PACKAGE = ConjurePackage.of("package");
    private static final TypeName FOO = TypeName.of("Foo", PACKAGE);
    private static final TypeName BAR = TypeName.of("Bar", PACKAGE);
    private static final Optional<String> DOCS = Optional.of("docs");

    @Test
    public void testNoSelfRecursiveType() throws Exception {
        ObjectsDefinition imports = mock(ObjectsDefinition.class);
        when(imports.objects()).thenReturn(
                ImmutableList.of(
                        ObjectTypeDefinition.builder()
                                .typeName(FOO)
                                .putFields(FieldName.of("self"), FieldDefinition.of(LocalReferenceType.of(FOO), DOCS))
                                .build()
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
                ImmutableList.of(
                        ObjectTypeDefinition.builder()
                                .typeName(TypeName.of("Foo", ConjurePackage.of("bar")))
                                .putAllFields(ImmutableMap.of(
                                        FieldName.of("selfOptional"),
                                        FieldDefinition.of(OptionalType.of(LocalReferenceType.of(FOO)), DOCS),
                                        FieldName.of("selfMap"),
                                        FieldDefinition.of(MapType.of(LocalReferenceType.of(FOO),
                                                LocalReferenceType.of(FOO)), DOCS),
                                        FieldName.of("selfSet"),
                                        FieldDefinition.of(SetType.of(LocalReferenceType.of(FOO)), DOCS),
                                        FieldName.of("selfList"),
                                        FieldDefinition.of(ListType.of(LocalReferenceType.of(FOO)), DOCS)
                                )).build()
                )
        );

        ObjectsDefinitionValidator.NO_RECURSIVE_TYPES.validate(imports);
    }

    @Test
    public void testNoRecursiveCycleType() throws Exception {
        ObjectsDefinition imports = mock(ObjectsDefinition.class);
        when(imports.objects()).thenReturn(
                ImmutableList.of(
                        ObjectTypeDefinition.builder()
                                .typeName(FOO)
                                .putFields(FieldName.of("bar"), field("Bar"))
                                .build(),
                        ObjectTypeDefinition.builder()
                                .typeName(BAR)
                                .putFields(FieldName.of("foo"), field("Foo"))
                                .build()
                )
        );

        assertThatThrownBy(() -> ObjectsDefinitionValidator.NO_RECURSIVE_TYPES.validate(imports))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageStartingWith("Illegal recursive data type: ");
    }

    @Test
    public void testNoComplexKeysInMaps() throws Exception {
        ObjectsDefinition imports = mock(ObjectsDefinition.class);
        String illegalFieldName = "asdf";
        ConjureType complexKeyType = ListType.of(PrimitiveType.STRING);
        List<BaseObjectTypeDefinition> objects = ImmutableList.of(
                ObjectTypeDefinition.builder()
                        .typeName(FOO)
                        .putFields(FieldName.of(illegalFieldName),
                                FieldDefinition.of(MapType.of(complexKeyType, PrimitiveType.STRING), DOCS))
                        .build());
        when(imports.objects()).thenReturn(objects);

        assertThatThrownBy(() -> ObjectsDefinitionValidator.NO_COMPLEX_KEYS.validate(imports))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(illegalFieldName)
                .hasMessageContaining(complexKeyType.toString());
    }

    private FieldDefinition field(String type) throws ParseException {
        return FieldDefinition.of(LocalReferenceType.of(TypeName.of(type, PACKAGE)), DOCS);
    }
}
