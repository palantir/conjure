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

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableList;
import com.palantir.conjure.spec.AliasDefinition;
import com.palantir.conjure.spec.ConjureDefinition;
import com.palantir.conjure.spec.Documentation;
import com.palantir.conjure.spec.EndpointDefinition;
import com.palantir.conjure.spec.EndpointName;
import com.palantir.conjure.spec.ExternalReference;
import com.palantir.conjure.spec.FieldDefinition;
import com.palantir.conjure.spec.FieldName;
import com.palantir.conjure.spec.HttpMethod;
import com.palantir.conjure.spec.HttpPath;
import com.palantir.conjure.spec.ListType;
import com.palantir.conjure.spec.LogSafety;
import com.palantir.conjure.spec.MapType;
import com.palantir.conjure.spec.ObjectDefinition;
import com.palantir.conjure.spec.OptionalType;
import com.palantir.conjure.spec.PrimitiveType;
import com.palantir.conjure.spec.ServiceDefinition;
import com.palantir.conjure.spec.SetType;
import com.palantir.conjure.spec.Type;
import com.palantir.conjure.spec.TypeDefinition;
import com.palantir.conjure.spec.TypeName;
import org.junit.jupiter.api.Test;

public class ConjureSourceFileValidatorTest {
    private static final String PACKAGE = "package";
    private static final TypeName FOO = TypeName.of("Foo", PACKAGE);
    private static final TypeName BAR = TypeName.of("Bar", PACKAGE);
    private static final Documentation DOCS = Documentation.of("docs");

    @Test
    public void testNoSelfRecursiveType() {
        ConjureDefinition conjureDef = ConjureDefinition.builder()
                .version(1)
                .types(ImmutableList.of(TypeDefinition.object(ObjectDefinition.builder()
                        .typeName(FOO)
                        .fields(FieldDefinition.builder()
                                .fieldName(FieldName.of("self"))
                                .type(Type.reference(FOO))
                                .docs(DOCS)
                                .build())
                        .build())))
                .build();

        assertThatThrownBy(() -> ConjureDefinitionValidator.NO_RECURSIVE_TYPES.validate(conjureDef))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Illegal recursive data type: Foo -> Foo");
    }

    @Test
    public void testRecursiveTypeOkInReference() {
        Type referenceType = Type.reference(FOO);
        TypeDefinition objectDefinition = TypeDefinition.object(ObjectDefinition.builder()
                .typeName(TypeName.of("Foo", "bar"))
                .addAllFields(ImmutableList.of(
                        FieldDefinition.builder()
                                .fieldName(FieldName.of("selfOptional"))
                                .type(Type.optional(OptionalType.of(Type.reference(FOO))))
                                .docs(DOCS)
                                .build(),
                        FieldDefinition.builder()
                                .fieldName(FieldName.of("selfMap"))
                                .type(Type.map(MapType.of(referenceType, referenceType)))
                                .docs(DOCS)
                                .build(),
                        FieldDefinition.builder()
                                .fieldName(FieldName.of("selfSet"))
                                .type(Type.set(SetType.of(referenceType)))
                                .docs(DOCS)
                                .build(),
                        FieldDefinition.builder()
                                .fieldName(FieldName.of("selfList"))
                                .type(Type.list(ListType.of(referenceType)))
                                .docs(DOCS)
                                .build()))
                .build());
        ConjureDefinition conjureDef = ConjureDefinition.builder()
                .version(1)
                .types(ImmutableList.of(objectDefinition))
                .build();

        ConjureDefinitionValidator.NO_RECURSIVE_TYPES.validate(conjureDef);
    }

    @Test
    public void testNoRecursiveCycleType() {
        ConjureDefinition conjureDef = ConjureDefinition.builder()
                .version(1)
                .types(ImmutableList.of(
                        TypeDefinition.object(ObjectDefinition.builder()
                                .typeName(FOO)
                                .fields(field(FieldName.of("bar"), "Bar"))
                                .build()),
                        TypeDefinition.object(ObjectDefinition.builder()
                                .typeName(BAR)
                                .fields(field(FieldName.of("foo"), "Foo"))
                                .build())))
                .build();

        assertThatThrownBy(() -> ConjureDefinitionValidator.NO_RECURSIVE_TYPES.validate(conjureDef))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageStartingWith("Illegal recursive data type: ");
    }

    @Test
    public void testNoIllegalMapKeys_returns() {
        ConjureDefinition conjureDef = ConjureDefinition.builder()
                .version(1)
                .services(ServiceDefinition.builder()
                        .serviceName(TypeName.of("name", "package"))
                        .endpoints(EndpointDefinition.builder()
                                .endpointName(EndpointName.of("badEndpoint"))
                                .httpMethod(HttpMethod.GET)
                                .httpPath(HttpPath.of("/"))
                                .returns(Type.map(MapType.of(
                                        Type.list(ListType.of(Type.primitive(PrimitiveType.STRING))),
                                        Type.primitive(PrimitiveType.STRING))))
                                .build())
                        .build())
                .build();

        assertThatThrownBy(() -> ConjureDefinitionValidator.ILLEGAL_MAP_KEYS.validate(conjureDef))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageStartingWith("Illegal map key found in return type of endpoint badEndpoint");
    }

    @Test
    public void testNoIllegalMapKeys_field() {
        ConjureDefinition conjureDef = ConjureDefinition.builder()
                .version(1)
                .types(TypeDefinition.object(ObjectDefinition.builder()
                        .typeName(FOO)
                        .fields(FieldDefinition.builder()
                                .fieldName(FieldName.of("bad"))
                                .type(Type.map(MapType.of(
                                        Type.list(ListType.of(Type.primitive(PrimitiveType.STRING))),
                                        Type.primitive(PrimitiveType.STRING))))
                                .docs(DOCS)
                                .build())
                        .build()))
                .build();
        assertThatThrownBy(() -> ConjureDefinitionValidator.ILLEGAL_MAP_KEYS.validate(conjureDef))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageStartingWith("Illegal map key found in object Foo");
    }

    @Test
    public void testNoIllegalMapKeys_intermediateAlias() {
        ConjureDefinition conjureDef = ConjureDefinition.builder()
                .version(1)
                .types(TypeDefinition.alias(AliasDefinition.builder()
                        .typeName(TypeName.of("AliasName", "package"))
                        .alias(Type.list(ListType.of(Type.primitive(PrimitiveType.STRING))))
                        .build()))
                .types(TypeDefinition.object(ObjectDefinition.builder()
                        .typeName(FOO)
                        .fields(FieldDefinition.builder()
                                .fieldName(FieldName.of("bad"))
                                .type(Type.map(MapType.of(
                                        Type.list(ListType.of(Type.primitive(PrimitiveType.STRING))),
                                        Type.primitive(PrimitiveType.STRING))))
                                .docs(DOCS)
                                .build())
                        .build()))
                .build();
        assertThatThrownBy(() -> ConjureDefinitionValidator.ILLEGAL_MAP_KEYS.validate(conjureDef))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageStartingWith("Illegal map key found in object Foo");
    }

    @Test
    public void testNoIllegalMapKeys_allowsExternalImport() {
        ConjureDefinition conjureDef = ConjureDefinition.builder()
                .version(1)
                .types(TypeDefinition.object(ObjectDefinition.builder()
                        .typeName(FOO)
                        .fields(FieldDefinition.builder()
                                .fieldName(FieldName.of("bad"))
                                .type(Type.map(MapType.of(
                                        Type.external(ExternalReference.builder()
                                                .externalReference(TypeName.of("Foo", "package"))
                                                .fallback(Type.primitive(PrimitiveType.STRING))
                                                .build()),
                                        Type.primitive(PrimitiveType.STRING))))
                                .docs(DOCS)
                                .build())
                        .build()))
                .build();
        assertThatCode(() -> ConjureDefinitionValidator.ILLEGAL_MAP_KEYS.validate(conjureDef))
                .describedAs("External imports may be used as map keys provided the fallback type is valid")
                .doesNotThrowAnyException();
    }

    @Test
    public void testNoIllegalMapKeys_faileInvalidExternalImport() {
        ConjureDefinition conjureDef = ConjureDefinition.builder()
                .version(1)
                .types(TypeDefinition.object(ObjectDefinition.builder()
                        .typeName(FOO)
                        .fields(FieldDefinition.builder()
                                .fieldName(FieldName.of("bad"))
                                .type(Type.map(MapType.of(
                                        Type.external(ExternalReference.builder()
                                                .externalReference(TypeName.of("Foo", "package"))
                                                .fallback(Type.primitive(PrimitiveType.ANY))
                                                .build()),
                                        Type.primitive(PrimitiveType.STRING))))
                                .docs(DOCS)
                                .build())
                        .build()))
                .build();
        assertThatThrownBy(() -> ConjureDefinitionValidator.ILLEGAL_MAP_KEYS.validate(conjureDef))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageStartingWith("Illegal map key found in object Foo");
    }

    @Test
    public void testNoSafetyOnComplexTypes() {
        ConjureDefinition conjureDef = ConjureDefinition.builder()
                .version(1)
                .types(TypeDefinition.alias(AliasDefinition.builder()
                        .typeName(TypeName.of("AliasName", "package"))
                        .alias(Type.list(ListType.of(Type.primitive(PrimitiveType.STRING))))
                        .build()))
                .types(TypeDefinition.object(ObjectDefinition.builder()
                        .typeName(FOO)
                        .fields(FieldDefinition.builder()
                                .fieldName(FieldName.of("bad"))
                                .type(Type.reference(TypeName.of("AliasName", "package")))
                                .safety(LogSafety.UNSAFE)
                                .docs(DOCS)
                                .build())
                        .build()))
                .build();
        assertThatThrownBy(() -> ConjureDefinitionValidator.validateAll(conjureDef))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(
                        "Only conjure primitives and wrappers around conjure primitives may declare safety");
    }

    @Test
    public void testSafetyOnBearerToken() {
        ConjureDefinition conjureDef = ConjureDefinition.builder()
                .version(1)
                .types(TypeDefinition.object(ObjectDefinition.builder()
                        .typeName(FOO)
                        .fields(FieldDefinition.builder()
                                .fieldName(FieldName.of("bad"))
                                .type(Type.primitive(PrimitiveType.BEARERTOKEN))
                                .safety(LogSafety.DO_NOT_LOG)
                                .docs(DOCS)
                                .build())
                        .build()))
                .build();
        assertThatThrownBy(() -> ConjureDefinitionValidator.validateAll(conjureDef))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("do-not-log by default and cannot be configured");
    }

    @Test
    public void testSafetyOnMap() {
        ConjureDefinition conjureDef = ConjureDefinition.builder()
                .version(1)
                .types(TypeDefinition.object(ObjectDefinition.builder()
                        .typeName(FOO)
                        .fields(FieldDefinition.builder()
                                .fieldName(FieldName.of("bad"))
                                .type(Type.map(MapType.of(
                                        Type.primitive(PrimitiveType.STRING), Type.primitive(PrimitiveType.STRING))))
                                .safety(LogSafety.DO_NOT_LOG)
                                .docs(DOCS)
                                .build())
                        .build()))
                .build();
        assertThatThrownBy(() -> ConjureDefinitionValidator.validateAll(conjureDef))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Maps are not supported at this time");
    }

    @Test
    public void testNoSafetyOnPrimitive() {
        ConjureDefinition conjureDef = ConjureDefinition.builder()
                .version(1)
                .types(TypeDefinition.object(ObjectDefinition.builder()
                        .typeName(FOO)
                        .fields(FieldDefinition.builder()
                                .fieldName(FieldName.of("bad"))
                                .type(Type.primitive(PrimitiveType.STRING))
                                .safety(LogSafety.UNSAFE)
                                .docs(DOCS)
                                .build())
                        .build()))
                .build();
        ConjureDefinitionValidator.validateAll(conjureDef);
    }

    private FieldDefinition field(FieldName name, String type) {
        return FieldDefinition.builder()
                .fieldName(name)
                .type(Type.reference(TypeName.of(type, PACKAGE)))
                .docs(DOCS)
                .build();
    }
}
