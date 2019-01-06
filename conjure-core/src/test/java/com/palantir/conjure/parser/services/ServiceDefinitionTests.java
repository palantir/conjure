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

package com.palantir.conjure.parser.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.palantir.conjure.parser.ConjureParser;
import com.palantir.conjure.parser.ConjureSourceFile;
import com.palantir.conjure.parser.types.BaseObjectTypeDefinition;
import com.palantir.conjure.parser.types.NamedTypesDefinition;
import com.palantir.conjure.parser.types.TypesDefinition;
import com.palantir.conjure.parser.types.builtin.AnyType;
import com.palantir.conjure.parser.types.collect.MapType;
import com.palantir.conjure.parser.types.complex.EnumTypeDefinition;
import com.palantir.conjure.parser.types.complex.EnumValueDefinition;
import com.palantir.conjure.parser.types.complex.FieldDefinition;
import com.palantir.conjure.parser.types.complex.ObjectTypeDefinition;
import com.palantir.conjure.parser.types.complex.UnionTypeDefinition;
import com.palantir.conjure.parser.types.names.ConjurePackage;
import com.palantir.conjure.parser.types.names.FieldName;
import com.palantir.conjure.parser.types.names.TypeName;
import com.palantir.conjure.parser.types.primitive.PrimitiveType;
import com.palantir.conjure.parser.types.reference.AliasTypeDefinition;
import com.palantir.conjure.parser.types.reference.ExternalTypeDefinition;
import com.palantir.conjure.parser.types.reference.LocalReferenceType;
import com.palantir.parsec.ParseException;
import java.io.File;
import java.io.IOException;
import org.junit.Test;

public final class ServiceDefinitionTests {

    private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory())
            .registerModule(new Jdk8Module());

    @Test
    public void testArgumentDefinition_fromString() throws IOException {
        assertThat(mapper.readValue("string", ArgumentDefinition.class))
                .isEqualTo(ArgumentDefinition.of(PrimitiveType.STRING));
    }

    @Test
    public void testArgumentDefinition_withDocs() throws IOException {
        assertThat(mapper.readValue(multiLineString(
                "type: string",
                "docs: docs"), ArgumentDefinition.class))
                .isEqualTo(ArgumentDefinition.of(PrimitiveType.STRING, "docs"));
    }

    @Test
    public void testEndpointDefinition_optionals() throws IOException {
        assertThat(mapper.readValue("http: GET /", EndpointDefinition.class))
                .isEqualTo(EndpointDefinition.builder()
                        .http(RequestLineDefinition.of("GET", PathString.of("/")))
                        .build());
    }

    @Test
    public void testEndpointDefinition_fullySpecified() throws IOException, ParseException {
        assertThat(mapper.readValue(multiLineString(
                "http: GET /{foo}/v1.2/path",
                "auth: header",
                "args:",
                "  arg:",
                "    type: string",
                "    docs: foo-docs",
                "    param-id: foo",
                "    param-type: path",
                "returns: string",
                "docs: |",
                "  docs"), EndpointDefinition.class))
                .isEqualTo(EndpointDefinition.builder()
                        .http(RequestLineDefinition.of("GET", PathString.of("/{foo}/v1.2/path")))
                        .auth(AuthDefinition.header())
                        .args(ImmutableMap.of(
                                ParameterName.of("arg"), ArgumentDefinition.builder()
                                        .type(PrimitiveType.STRING)
                                        .docs("foo-docs")
                                        .paramId(ParameterName.of("foo"))
                                        .paramType(ArgumentDefinition.ParamType.PATH)
                                        .build()))
                        .returns(PrimitiveType.STRING)
                        .docs("docs")
                        .build());
    }

    @Test
    public void testParseEnum_baseCase() throws IOException {
        assertThat(mapper.readValue(multiLineString(
                "values:",
                " - A",
                " - B"), BaseObjectTypeDefinition.class))
                .isEqualTo(EnumTypeDefinition.builder()
                        .addValues(EnumValueDefinition.builder().value("A").build())
                        .addValues(EnumValueDefinition.builder().value("B").build())
                        .build());
    }

    @Test
    public void testParseEnum_blank() {
        assertThatThrownBy(() -> mapper.readValue(multiLineString(
                "values:"), BaseObjectTypeDefinition.class))
                .hasMessageContaining("Invalid `null` value encountered for property \"values\"");

    }

    @Test
    public void testParseEnum_empty() throws IOException {
        assertThat(mapper.readValue(multiLineString(
                "values: []"), BaseObjectTypeDefinition.class))
                .isEqualTo(EnumTypeDefinition.builder().build());
    }

    @Test
    public void testParseEnum_null() {
        assertThatThrownBy(() -> mapper.readValue(multiLineString(
                "values: null"), BaseObjectTypeDefinition.class))
                .hasMessageContaining("Invalid `null` value encountered for property \"values\"");
    }

    @Test
    public void testParseEnum_withObjectDocs() throws IOException {
        assertThat(mapper.readValue(multiLineString(
                "docs: Test",
                "values:",
                " - A",
                " - B"), BaseObjectTypeDefinition.class))
                .isEqualTo(EnumTypeDefinition.builder()
                        .addValues(EnumValueDefinition.builder().value("A").build())
                        .addValues(EnumValueDefinition.builder().value("B").build())
                        .docs("Test")
                        .build());
    }

    @Test
    public void testParseEnum_withValueDocs() throws IOException {
        assertThat(mapper.readValue(multiLineString(
                "docs: Test",
                "values:",
                " - value: A",
                "   docs: A docs",
                " - B"), BaseObjectTypeDefinition.class))
                .isEqualTo(EnumTypeDefinition.builder()
                        .addValues(EnumValueDefinition.builder().value("A").docs("A docs").build())
                        .addValues(EnumValueDefinition.builder().value("B").build())
                        .docs("Test")
                        .build());
    }

    @Test
    public void testParseUnion_blank() {
        assertThatThrownBy(() -> mapper.readValue(multiLineString(
                "union:"), BaseObjectTypeDefinition.class))
                .hasMessageContaining("Invalid `null` value encountered for property \"union\"");
    }

    @Test
    public void testParseUnion_empty() throws IOException {
        assertThat(mapper.readValue(multiLineString(
                "union: {}"), BaseObjectTypeDefinition.class))
                .isEqualTo(UnionTypeDefinition.builder().build());
    }

    @Test
    public void testParseUnion_null() {
        assertThatThrownBy(() -> mapper.readValue(multiLineString(
                "union: null"), BaseObjectTypeDefinition.class))
                .hasMessageContaining("Invalid `null` value encountered for property \"union\"");
    }

    @Test
    public void testParseUnion_withObjectDocs() throws IOException {
        assertThat(mapper.readValue(multiLineString(
                "docs: Test",
                "union:",
                "  stringId: string",
                "  numberId: integer"), BaseObjectTypeDefinition.class))
                .isEqualTo(UnionTypeDefinition.builder()
                        .putUnion(FieldName.of("stringId"), FieldDefinition.of(PrimitiveType.STRING))
                        .putUnion(FieldName.of("numberId"), FieldDefinition.of(PrimitiveType.INTEGER))
                        .docs("Test")
                        .build());
    }

    @Test
    public void testParseType_blank() {
        assertThatThrownBy(() -> mapper.readValue(multiLineString(
                "fields:"), BaseObjectTypeDefinition.class))
                .hasMessageContaining("Invalid `null` value encountered for property \"fields\"");
    }

    @Test
    public void testParseType_empty() throws IOException {
        assertThat(mapper.readValue(multiLineString(
                "fields: {}"), BaseObjectTypeDefinition.class))
                .isEqualTo(ObjectTypeDefinition.builder().build());
    }

    @Test
    public void testParseType_null() {
        assertThatThrownBy(() -> mapper.readValue(multiLineString(
                "fields:"), BaseObjectTypeDefinition.class))
                .hasMessageContaining("Invalid `null` value encountered for property \"fields\"");
    }

    @Test
    public void testParseType_withObjectDocs() throws IOException {
        assertThat(mapper.readValue(multiLineString(
                "docs: Test",
                "fields:",
                "  name: string",
                "  age: integer"), BaseObjectTypeDefinition.class))
                .isEqualTo(ObjectTypeDefinition.builder()
                        .putFields(FieldName.of("name"), FieldDefinition.of(PrimitiveType.STRING))
                        .putFields(FieldName.of("age"), FieldDefinition.of(PrimitiveType.INTEGER))
                        .docs("Test")
                        .build());
    }

    @Test
    public void testParseAlias_validPrimitiveType() throws IOException {
        assertThat(mapper.readValue("alias: string", BaseObjectTypeDefinition.class))
                .isEqualTo(AliasTypeDefinition.builder().alias(PrimitiveType.STRING).build());
    }

    @Test
    public void testParseAlias_validMapType() throws IOException {
        assertThat(mapper.readValue("alias: map<string, any>", BaseObjectTypeDefinition.class))
                .isEqualTo(AliasTypeDefinition.builder().alias(MapType.of(PrimitiveType.STRING, AnyType.of())).build());
    }

    @Test
    public void testParseAlias_validReference() throws IOException {
        assertThat(mapper.readValue("alias: Foo", BaseObjectTypeDefinition.class))
                .isEqualTo(AliasTypeDefinition.builder().alias(LocalReferenceType.of(TypeName.of("Foo"))).build());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testParseConjureFile() throws IOException {
        ConjureSourceFile def = ConjureParser.parse(new File("src/test/resources/test-service.yml"));
        assertThat(def).isEqualTo(
                ConjureSourceFile.builder()
                        .types(TypesDefinition.builder()
                                .putImports(TypeName.of("ResourceIdentifier"),
                                        ExternalTypeDefinition.javaType(
                                                "com.palantir.ri.ResourceIdentifier", PrimitiveType.STRING))
                                .definitions(NamedTypesDefinition.builder()
                                        .defaultConjurePackage(ConjurePackage.of("test.api"))
                                        .putObjects(TypeName.of("SimpleObject"), ObjectTypeDefinition.builder()
                                                .putFields(FieldName.of("stringField"),
                                                        FieldDefinition.of(PrimitiveType.STRING))
                                                .build())
                                        .putObjects(TypeName.of("StringAlias"), AliasTypeDefinition.builder()
                                                .alias(PrimitiveType.STRING)
                                                .build())
                                        .build())
                                .build())
                        .putServices(TypeName.of("TestService"), ServiceDefinition.builder()
                                .doNotUseName("Test Service")
                                .conjurePackage(ConjurePackage.of("test.api"))
                                .putEndpoints("get", EndpointDefinition.builder()
                                        .http(RequestLineDefinition.of("GET", PathString.of("/get")))
                                        .build())
                                .putEndpoints("post", EndpointDefinition.builder()
                                        .http(RequestLineDefinition.of("POST", PathString.of("/post")))
                                        .args(ImmutableMap.of(ParameterName.of("foo"), ArgumentDefinition.builder()
                                                .paramType(ArgumentDefinition.ParamType.HEADER)
                                                .type(LocalReferenceType.of(TypeName.of("StringAlias")))
                                                .build()))
                                        .build())
                                .build())
                        .build());
    }

    private static String multiLineString(String... lines) {
        return Joiner.on('\n').join(lines);
    }

}
