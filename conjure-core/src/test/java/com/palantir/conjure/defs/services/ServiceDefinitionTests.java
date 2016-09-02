/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.services;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.palantir.conjure.defs.Conjure;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.defs.ObjectsDefinition;
import com.palantir.conjure.defs.TypesDefinition;
import com.palantir.conjure.defs.types.AliasTypeDefinition;
import com.palantir.conjure.defs.types.BaseObjectTypeDefinition;
import com.palantir.conjure.defs.types.EnumTypeDefinition;
import com.palantir.conjure.defs.types.EnumValueDefinition;
import com.palantir.conjure.defs.types.ExternalTypeDefinition;
import com.palantir.conjure.defs.types.FieldDefinition;
import com.palantir.conjure.defs.types.ObjectTypeDefinition;
import com.palantir.conjure.defs.types.PrimitiveType;
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
                        .http(RequestLineDefinition.of("GET", "/"))
                        .build());
    }

    @Test
    public void testEndpointDefinition_fullySpecified() throws IOException {
        assertThat(mapper.readValue(multiLineString(
                "http: GET /{arg}",
                "auth: header",
                "args:",
                "  arg: string",
                "returns: string",
                "docs: |",
                "  docs"), EndpointDefinition.class))
                .isEqualTo(EndpointDefinition.builder()
                        .http(RequestLineDefinition.of("GET", "/{arg}"))
                        .auth(AuthDefinition.header())
                        .args(ImmutableMap.of("arg", ArgumentDefinition.of(PrimitiveType.STRING)))
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
    public void testParseEnum_empty() throws IOException {
        assertThat(mapper.readValue(multiLineString(
                "values:"), BaseObjectTypeDefinition.class))
                .isEqualTo(EnumTypeDefinition.builder().build());
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
    public void testParseEnum_usesUnknown() throws IOException {
        try {
            mapper.readValue(multiLineString(
                    "values:",
                    " - A",
                    " - Unknown"), BaseObjectTypeDefinition.class);
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage("UNKNOWN is a reserved enumeration value");
        }
    }

    @Test
    public void testParseAlias_validPrimitiveType() throws IOException {
        assertThat(mapper.readValue("alias: string", BaseObjectTypeDefinition.class))
                .isEqualTo(AliasTypeDefinition.builder().alias(PrimitiveType.STRING).build());
    }

    @Test
    public void testParseAlias_notAPrimitiveType() throws IOException {
        try {
            mapper.readValue("alias: bummer", BaseObjectTypeDefinition.class);
            fail();
        } catch (JsonMappingException e) {
            // TODO(melliot) improve error reporting
            assertThat(e.getMessage()).startsWith(
                    "Can not construct instance of com.palantir.conjure.defs.types.PrimitiveType, "
                            + "problem: Unknown primitive type: bummer");
        }
    }

    @Test
    public void testParseEnum_illegalFormat() throws IOException {
        try {
            mapper.readValue(multiLineString(
                    "values:",
                    " - a",
                    " - a_b",
                    " - A__B",
                    " - _A",
                    " - A_"), BaseObjectTypeDefinition.class);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage("Enumeration values must have format [A-Z]+(_[A-Z]+)*, illegal values: "
                    + "[a, _A, a_b, A_, A__B]");
        }
    }

    @Test
    public void testParseConjureFile() throws IOException {
        ConjureDefinition def = Conjure.parse(new File("src/test/resources/test-service.yml"));
        assertThat(def).isEqualTo(
                ConjureDefinition.builder()
                .types(TypesDefinition.builder()
                        .putImports("ResourceIdentifier",
                                ExternalTypeDefinition.javaType("com.palantir.ri.ResourceIdentifier"))
                        .definitions(ObjectsDefinition.builder()
                                .defaultPackage("test.api")
                                .putObjects("SimpleObject", ObjectTypeDefinition.builder()
                                        .putFields("stringField", FieldDefinition.of(PrimitiveType.STRING))
                                        .build())
                                .build())
                        .build())
                .putServices("TestService", ServiceDefinition.builder()
                        .name("Test Service")
                        .packageName("test.api")
                        .putEndpoints("get", EndpointDefinition.builder()
                                .http(RequestLineDefinition.of("GET", "/get"))
                                .build())
                        .build())
                .build());
    }

    private static String multiLineString(String... lines) {
        return Joiner.on('\n').join(lines);
    }

}
