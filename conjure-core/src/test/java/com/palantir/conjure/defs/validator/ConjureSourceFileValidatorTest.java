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
import com.palantir.conjure.defs.SafetyDeclarationRequirements;
import com.palantir.conjure.exceptions.ConjureIllegalStateException;
import com.palantir.conjure.spec.AliasDefinition;
import com.palantir.conjure.spec.ArgumentDefinition;
import com.palantir.conjure.spec.ArgumentName;
import com.palantir.conjure.spec.BodyParameterType;
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
import com.palantir.conjure.spec.ParameterType;
import com.palantir.conjure.spec.PrimitiveType;
import com.palantir.conjure.spec.ServiceDefinition;
import com.palantir.conjure.spec.SetType;
import com.palantir.conjure.spec.Type;
import com.palantir.conjure.spec.TypeDefinition;
import com.palantir.conjure.spec.TypeName;
import com.palantir.conjure.spec.UnionDefinition;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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
        assertThatThrownBy(
                        () -> ConjureDefinitionValidator.validateAll(conjureDef, SafetyDeclarationRequirements.ALLOWED))
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
        assertThatThrownBy(
                        () -> ConjureDefinitionValidator.validateAll(conjureDef, SafetyDeclarationRequirements.ALLOWED))
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
        assertThatThrownBy(
                        () -> ConjureDefinitionValidator.validateAll(conjureDef, SafetyDeclarationRequirements.ALLOWED))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContainingAll("Maps cannot declare log safety", "Consider using alias types");
    }

    @Test
    public void testInvalidSafetyArgument_bearertoken() {
        ConjureDefinition conjureDef = ConjureDefinition.builder()
                .version(1)
                .services(ServiceDefinition.builder()
                        .serviceName(TypeName.of("Service", "com.palantir.product"))
                        .endpoints(EndpointDefinition.builder()
                                .endpointName(EndpointName.of("end"))
                                .httpMethod(HttpMethod.PUT)
                                .httpPath(HttpPath.of("/path"))
                                .args(ArgumentDefinition.builder()
                                        .argName(ArgumentName.of("arg"))
                                        .type(Type.primitive(PrimitiveType.BEARERTOKEN))
                                        .paramType(ParameterType.body(BodyParameterType.of()))
                                        .safety(LogSafety.UNSAFE)
                                        .build())
                                .build())
                        .build())
                .build();
        assertThatThrownBy(
                        () -> ConjureDefinitionValidator.validateAll(conjureDef, SafetyDeclarationRequirements.ALLOWED))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(
                        "Service.end(arg): bearertoken values are do-not-log by default and cannot be configured");
    }

    @Test
    public void testInvalidSafetyArgument_reference() {
        ConjureDefinition conjureDef = ConjureDefinition.builder()
                .version(1)
                .types(TypeDefinition.object(
                        ObjectDefinition.builder().typeName(FOO).build()))
                .services(ServiceDefinition.builder()
                        .serviceName(TypeName.of("Service", "com.palantir.product"))
                        .endpoints(EndpointDefinition.builder()
                                .endpointName(EndpointName.of("end"))
                                .httpMethod(HttpMethod.PUT)
                                .httpPath(HttpPath.of("/path"))
                                .args(ArgumentDefinition.builder()
                                        .argName(ArgumentName.of("arg"))
                                        .type(Type.reference(FOO))
                                        .paramType(ParameterType.body(BodyParameterType.of()))
                                        .safety(LogSafety.UNSAFE)
                                        .build())
                                .build())
                        .build())
                .build();
        assertThatThrownBy(
                        () -> ConjureDefinitionValidator.validateAll(conjureDef, SafetyDeclarationRequirements.ALLOWED))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(
                        "Service.end(arg): end cannot declare log safety. Only conjure primitives and wrappers around"
                                + " conjure primitives may declare safety. package.Foo is not a primitive type.");
    }

    @Test
    public void testSafetyTagsAreNotAllowedWhenSafetyIsRequired() {
        ConjureDefinition conjureDef = ConjureDefinition.builder()
                .version(1)
                .types(TypeDefinition.alias(AliasDefinition.builder()
                        .typeName(TypeName.of("AliasName", "package"))
                        .alias(Type.list(ListType.of(Type.primitive(PrimitiveType.STRING))))
                        .safety(LogSafety.SAFE)
                        .build()))
                .services(ServiceDefinition.builder()
                        .serviceName(TypeName.of("Service", "com.palantir.product"))
                        .endpoints(EndpointDefinition.builder()
                                .endpointName(EndpointName.of("end"))
                                .httpMethod(HttpMethod.PUT)
                                .httpPath(HttpPath.of("/path"))
                                .args(ArgumentDefinition.builder()
                                        .argName(ArgumentName.of("arg"))
                                        .type(Type.reference(TypeName.of("AliasName", "package")))
                                        .paramType(ParameterType.body(BodyParameterType.of()))
                                        .tags("safe")
                                        .build())
                                .build())
                        .build())
                .build();
        assertThatCode(() -> ConjureDefinitionValidator.validateAll(conjureDef, SafetyDeclarationRequirements.ALLOWED))
                .doesNotThrowAnyException();
        assertThatThrownBy(() ->
                        ConjureDefinitionValidator.validateAll(conjureDef, SafetyDeclarationRequirements.REQUIRED))
                .isInstanceOf(ConjureIllegalStateException.class)
                .hasMessageContaining("Service.end(arg): Safety tags have been replaced by the 'safety' field");
    }

    @Test
    public void testSafetyMarkersAreNotAllowedWhenSafetyIsRequired() {
        ConjureDefinition conjureDef = ConjureDefinition.builder()
                .version(1)
                .types(TypeDefinition.alias(AliasDefinition.builder()
                        .typeName(TypeName.of("AliasName", "package"))
                        .alias(Type.list(ListType.of(Type.primitive(PrimitiveType.STRING))))
                        .safety(LogSafety.SAFE)
                        .build()))
                .services(ServiceDefinition.builder()
                        .serviceName(TypeName.of("Service", "com.palantir.product"))
                        .endpoints(EndpointDefinition.builder()
                                .endpointName(EndpointName.of("end"))
                                .httpMethod(HttpMethod.PUT)
                                .httpPath(HttpPath.of("/path"))
                                .args(ArgumentDefinition.builder()
                                        .argName(ArgumentName.of("arg"))
                                        .type(Type.reference(TypeName.of("AliasName", "package")))
                                        .paramType(ParameterType.body(BodyParameterType.of()))
                                        .markers(Type.external(ExternalReference.builder()
                                                .externalReference(TypeName.of("Safe", "com.palantir.logsafe"))
                                                .fallback(Type.primitive(PrimitiveType.STRING))
                                                .safety(Optional.empty())
                                                .build()))
                                        .build())
                                .build())
                        .build())
                .build();
        assertThatCode(() -> ConjureDefinitionValidator.validateAll(conjureDef, SafetyDeclarationRequirements.ALLOWED))
                .doesNotThrowAnyException();
        assertThatThrownBy(() ->
                        ConjureDefinitionValidator.validateAll(conjureDef, SafetyDeclarationRequirements.REQUIRED))
                .isInstanceOf(ConjureIllegalStateException.class)
                .hasMessageContaining("Service.end(arg): Safety markers have been replaced by the 'safety' field");
    }

    private static Stream<Arguments> provideExternalImports_InvalidTypes() {
        Type badExternalReference = Type.external(ExternalReference.builder()
                .externalReference(TypeName.of("weirdType", "java.lang"))
                .fallback(Type.primitive(PrimitiveType.INTEGER))
                .safety(LogSafety.DO_NOT_LOG)
                .build());
        return getAllTypesToTest_SafetyAtImportTime(badExternalReference);
    }

    @ParameterizedTest
    @MethodSource("provideExternalImports_InvalidTypes")
    public void testSafetyExternalImport_InvalidType(ConjureDefinition definition) {
        assertThatThrownBy(
                        () -> ConjureDefinitionValidator.validateAll(definition, SafetyDeclarationRequirements.ALLOWED))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContainingAll("cannot declare log safety", "java.lang.weirdType is not a primitive type.");
    }

    private static Stream<Arguments> provideExternalImports_InvalidFallback() {
        Type badExternalReference = Type.external(ExternalReference.builder()
                .externalReference(TypeName.of("Long", "java.lang"))
                .fallback(Type.primitive(PrimitiveType.INTEGER))
                .safety(LogSafety.DO_NOT_LOG)
                .build());
        return getAllTypesToTest_SafetyAtImportTime(badExternalReference);
    }

    @ParameterizedTest
    @MethodSource("provideExternalImports_InvalidFallback")
    public void testSafetyExternalImport_InvalidFallback(ConjureDefinition definition) {
        assertThatThrownBy(
                        () -> ConjureDefinitionValidator.validateAll(definition, SafetyDeclarationRequirements.ALLOWED))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(
                        "Mismatched base type. java.lang.Long must have a base type of string in order to declare"
                                + " safety.");
    }

    private static Stream<Arguments> provideExternalImports_NotAtImportTime() {
        Type external = Type.external(ExternalReference.builder()
                .externalReference(TypeName.of("Long", "java.lang"))
                .fallback(Type.primitive(PrimitiveType.INTEGER))
                .build());
        return getAllTypesToTest_SafetyNotAtImportTime(external);
    }

    @ParameterizedTest
    @MethodSource("provideExternalImports_NotAtImportTime")
    public void testSafetyExternalImport_NotAtImportTime(ConjureDefinition definition) {
        assertThatThrownBy(() -> ConjureDefinitionValidator.validateAll(
                        definition, SafetyDeclarationRequirements.EXTERNAL_IMPORTS_REQUIRED))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("External types must declare safety at import time");
    }

    private static Stream<Arguments> providesExternalImports_WithSafety() {
        Type external = Type.external(ExternalReference.builder()
                .externalReference(TypeName.of("Long", "java.lang"))
                .fallback(Type.primitive(PrimitiveType.STRING))
                .safety(LogSafety.DO_NOT_LOG)
                .build());
        return getAllTypesToTest_SafetyAtImportTime(external);
    }

    private static Stream<Arguments> providesExternalImports_WithoutSafety() {
        Type external = Type.external(ExternalReference.builder()
                .externalReference(TypeName.of("Long", "java.lang"))
                .fallback(Type.primitive(PrimitiveType.STRING))
                .build());
        return getAllTypesToTest_SafetyAtImportTime(external);
    }

    private static Stream<Arguments> providesPrimitives_WithSafety() {
        Type primitive = Type.primitive(PrimitiveType.STRING);
        return getAllTypesToTest_SafetyNotAtImportTime(primitive);
    }

    private static Stream<Arguments> providesPrimitives_WithoutSafety() {
        Type primitive = Type.primitive(PrimitiveType.STRING);
        return getAllTypesToTest_SafetyAtImportTime(primitive);
    }

    @ParameterizedTest
    @MethodSource("providesExternalImports_WithSafety")
    public void testAllowedSafety_ExternalImportsGiven(ConjureDefinition definition) {
        ConjureDefinitionValidator.validateAll(definition, SafetyDeclarationRequirements.ALLOWED);
    }

    @ParameterizedTest
    @MethodSource("providesExternalImports_WithoutSafety")
    public void testAllowedSafety_ExternalImportsMissing(ConjureDefinition definition) {
        ConjureDefinitionValidator.validateAll(definition, SafetyDeclarationRequirements.ALLOWED);
    }

    @ParameterizedTest
    @MethodSource("providesPrimitives_WithSafety")
    public void testAllowedSafety_PrimitivesGiven(ConjureDefinition definition) {
        ConjureDefinitionValidator.validateAll(definition, SafetyDeclarationRequirements.ALLOWED);
    }

    @ParameterizedTest
    @MethodSource("providesPrimitives_WithoutSafety")
    public void testAllowedSafety_PrimitivesMissing(ConjureDefinition definition) {
        ConjureDefinitionValidator.validateAll(definition, SafetyDeclarationRequirements.ALLOWED);
    }

    @ParameterizedTest
    @MethodSource("providesExternalImports_WithSafety")
    public void testRequiredSafety_ExternalImportsGiven(ConjureDefinition definition) {
        ConjureDefinitionValidator.validateAll(definition, SafetyDeclarationRequirements.REQUIRED);
    }

    @ParameterizedTest
    @MethodSource("providesExternalImports_WithoutSafety")
    public void testRequiredSafety_ExternalImportsMissing(ConjureDefinition definition) {
        ConjureDefinitionValidator.validateAll(definition, SafetyDeclarationRequirements.REQUIRED);
    }

    @ParameterizedTest
    @MethodSource("providesPrimitives_WithSafety")
    public void testRequiredSafety_PrimitivesGiven(ConjureDefinition definition) {
        ConjureDefinitionValidator.validateAll(definition, SafetyDeclarationRequirements.REQUIRED);
    }

    @ParameterizedTest
    @MethodSource("providesPrimitives_WithoutSafety")
    public void testRequiredSafety_PrimitivesMissing(ConjureDefinition definition) {
        assertThatThrownBy(() ->
                        ConjureDefinitionValidator.validateAll(definition, SafetyDeclarationRequirements.REQUIRED))
                .isInstanceOf(ConjureIllegalStateException.class)
                .hasMessageContaining("must declare log safety");
    }

    @ParameterizedTest
    @MethodSource("providesExternalImports_WithSafety")
    public void testExternalImportsRequiredSafety_Given(ConjureDefinition definition) {
        ConjureDefinitionValidator.validateAll(definition, SafetyDeclarationRequirements.EXTERNAL_IMPORTS_REQUIRED);
    }

    @ParameterizedTest
    @MethodSource("providesExternalImports_WithoutSafety")
    public void testExternalImportsRequiredSafety_Missing(ConjureDefinition definition) {
        assertThatThrownBy(() -> ConjureDefinitionValidator.validateAll(
                        definition, SafetyDeclarationRequirements.EXTERNAL_IMPORTS_REQUIRED))
                .isInstanceOf(ConjureIllegalStateException.class)
                .hasMessageContaining("must declare log safety");
    }

    @ParameterizedTest
    @MethodSource("providesPrimitives_WithSafety")
    public void testExternalImportsRequiredSafety_PrimitivesGiven(ConjureDefinition definition) {
        ConjureDefinitionValidator.validateAll(definition, SafetyDeclarationRequirements.EXTERNAL_IMPORTS_REQUIRED);
    }

    @ParameterizedTest
    @MethodSource("providesPrimitives_WithoutSafety")
    public void testExternalImportsRequiredSafety_PrimitivesMissing(ConjureDefinition definition) {
        assertThatThrownBy(() -> ConjureDefinitionValidator.validateAll(
                        definition, SafetyDeclarationRequirements.EXTERNAL_IMPORTS_REQUIRED))
                .isInstanceOf(ConjureIllegalStateException.class)
                .hasMessageContaining("must declare log safety");
    }

    private FieldDefinition field(FieldName name, String type) {
        return FieldDefinition.builder()
                .fieldName(name)
                .type(Type.reference(TypeName.of(type, PACKAGE)))
                .docs(DOCS)
                .build();
    }

    private static Stream<Arguments> getAllTypesToTest_SafetyAtImportTime(Type externalReference) {
        ConjureDefinition conjureDefObject = ConjureDefinition.builder()
                .version(1)
                .types(TypeDefinition.object(ObjectDefinition.builder()
                        .typeName(FOO)
                        .fields(FieldDefinition.builder()
                                .fieldName(FieldName.of("bad"))
                                .type(externalReference)
                                .docs(DOCS)
                                .build())
                        .build()))
                .build();

        ConjureDefinition conjureDefAlias = ConjureDefinition.builder()
                .version(1)
                .types(TypeDefinition.alias(AliasDefinition.builder()
                        .typeName(FOO)
                        .alias(externalReference)
                        .build()))
                .build();

        ConjureDefinition conjureDefUnion = ConjureDefinition.builder()
                .version(1)
                .types(TypeDefinition.union(UnionDefinition.builder()
                        .union(FieldDefinition.builder()
                                .fieldName(FieldName.of("badImport"))
                                .type(externalReference)
                                .docs(DOCS)
                                .build())
                        .typeName(FOO)
                        .build()))
                .build();

        ConjureDefinition conjureDefEndpoint = ConjureDefinition.builder()
                .version(1)
                .services(ServiceDefinition.builder()
                        .serviceName(TypeName.of("name", "package"))
                        .endpoints(EndpointDefinition.builder()
                                .endpointName(EndpointName.of("badEndpoint"))
                                .httpMethod(HttpMethod.GET)
                                .httpPath(HttpPath.of("/"))
                                .args(ArgumentDefinition.builder()
                                        .argName(ArgumentName.of("testArgument"))
                                        .type(externalReference)
                                        .paramType(ParameterType.body(BodyParameterType.of()))
                                        .build())
                                .build())
                        .build())
                .build();

        return Stream.of(
                Arguments.of(Named.of("Object", conjureDefObject)),
                Arguments.of(Named.of("Alias", conjureDefAlias)),
                Arguments.of(Named.of("Union", conjureDefUnion)),
                Arguments.of(Named.of("Endpoint", conjureDefEndpoint)));
    }

    private static Stream<Arguments> getAllTypesToTest_SafetyNotAtImportTime(Type primitive) {
        ConjureDefinition conjureDefObject = ConjureDefinition.builder()
                .version(1)
                .types(TypeDefinition.object(ObjectDefinition.builder()
                        .typeName(FOO)
                        .fields(FieldDefinition.builder()
                                .fieldName(FieldName.of("bad"))
                                .type(primitive)
                                .safety(LogSafety.DO_NOT_LOG)
                                .docs(DOCS)
                                .build())
                        .build()))
                .build();

        ConjureDefinition conjureDefAlias = ConjureDefinition.builder()
                .version(1)
                .types(TypeDefinition.alias(AliasDefinition.builder()
                        .typeName(FOO)
                        .alias(primitive)
                        .safety(LogSafety.DO_NOT_LOG)
                        .build()))
                .build();

        ConjureDefinition conjureDefUnion = ConjureDefinition.builder()
                .version(1)
                .types(TypeDefinition.union(UnionDefinition.builder()
                        .union(FieldDefinition.builder()
                                .fieldName(FieldName.of("badImport"))
                                .type(primitive)
                                .safety(LogSafety.DO_NOT_LOG)
                                .docs(DOCS)
                                .build())
                        .typeName(FOO)
                        .build()))
                .build();

        ConjureDefinition conjureDefEndpoint = ConjureDefinition.builder()
                .version(1)
                .services(ServiceDefinition.builder()
                        .serviceName(TypeName.of("name", "package"))
                        .endpoints(EndpointDefinition.builder()
                                .endpointName(EndpointName.of("badEndpoint"))
                                .httpMethod(HttpMethod.GET)
                                .httpPath(HttpPath.of("/"))
                                .args(ArgumentDefinition.builder()
                                        .argName(ArgumentName.of("testArgument"))
                                        .type(primitive)
                                        .safety(LogSafety.DO_NOT_LOG)
                                        .paramType(ParameterType.body(BodyParameterType.of()))
                                        .build())
                                .build())
                        .build())
                .build();

        return Stream.of(
                Arguments.of(Named.of("Object", conjureDefObject)),
                Arguments.of(Named.of("Alias", conjureDefAlias)),
                Arguments.of(Named.of("Union", conjureDefUnion)),
                Arguments.of(Named.of("Endpoint", conjureDefEndpoint)));
    }
}
