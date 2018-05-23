/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.palantir.conjure.defs.validator;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableList;
import com.palantir.conjure.spec.ConjureDefinition;
import com.palantir.conjure.spec.Documentation;
import com.palantir.conjure.spec.FieldDefinition;
import com.palantir.conjure.spec.FieldName;
import com.palantir.conjure.spec.ListType;
import com.palantir.conjure.spec.MapType;
import com.palantir.conjure.spec.ObjectDefinition;
import com.palantir.conjure.spec.OptionalType;
import com.palantir.conjure.spec.SetType;
import com.palantir.conjure.spec.Type;
import com.palantir.conjure.spec.TypeDefinition;
import com.palantir.conjure.spec.TypeName;
import org.junit.Test;

public class ConjureDefinitionValidatorTest {
    private static final String PACKAGE = "package";
    private static final TypeName FOO = TypeName.of("Foo", PACKAGE);
    private static final TypeName BAR = TypeName.of("Bar", PACKAGE);
    private static final Documentation DOCS = Documentation.of("docs");

    @Test
    public void testNoSelfRecursiveType() {
        ConjureDefinition conjureDef = ConjureDefinition.builder().types(
                ImmutableList.of(TypeDefinition.object(
                        ObjectDefinition.builder()
                                .typeName(FOO)
                                .fields(FieldDefinition.of(FieldName.of("self"), Type.reference(FOO), DOCS))
                                .build())
                )).build();

        assertThatThrownBy(() -> ConjureDefinitionValidator.NO_RECURSIVE_TYPES.validate(conjureDef))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Illegal recursive data type: Foo -> Foo");
    }

    @Test
    public void testRecursiveTypeOkInReference() {
        Type referenceType = Type.reference(FOO);
        ConjureDefinition conjureDef = ConjureDefinition.builder().types(
                ImmutableList.of(TypeDefinition.object(
                        ObjectDefinition.builder()
                                .typeName(TypeName.of("Foo", "bar"))
                                .addAllFields(ImmutableList.of(
                                        FieldDefinition.of(FieldName.of("selfOptional"),
                                                Type.optional(OptionalType.of(Type.reference(FOO))), DOCS),
                                        FieldDefinition.of(FieldName.of("selfMap"),
                                                Type.map(MapType.of(referenceType, referenceType)), DOCS),
                                        FieldDefinition.of(FieldName.of("selfSet"),
                                                Type.set(SetType.of(referenceType)), DOCS),
                                        FieldDefinition.of(FieldName.of("selfList"),
                                                Type.list(ListType.of(referenceType)), DOCS)
                                )).build())
                )).build();

        ConjureDefinitionValidator.NO_RECURSIVE_TYPES.validate(conjureDef);
    }

    @Test
    public void testNoRecursiveCycleType() {
        ConjureDefinition conjureDef = ConjureDefinition.builder().types(
                ImmutableList.of(
                        TypeDefinition.object(
                                ObjectDefinition.builder()
                                        .typeName(FOO)
                                        .fields(field(FieldName.of("bar"), "Bar"))
                                        .build()),
                        TypeDefinition.object(
                                ObjectDefinition.builder()
                                        .typeName(BAR)
                                        .fields(field(FieldName.of("foo"), "Foo"))
                                        .build())
                )).build();

        assertThatThrownBy(() -> ConjureDefinitionValidator.NO_RECURSIVE_TYPES.validate(conjureDef))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageStartingWith("Illegal recursive data type: ");
    }

    private FieldDefinition field(FieldName name, String type) {
        return FieldDefinition.of(name, Type.reference(TypeName.of(type, PACKAGE)), DOCS);
    }
}
