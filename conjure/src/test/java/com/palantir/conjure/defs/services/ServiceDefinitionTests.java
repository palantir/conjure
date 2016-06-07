/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.services;

import static com.google.common.truth.Truth.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.palantir.conjure.defs.Conjure;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.defs.ObjectsDefinition;
import com.palantir.conjure.defs.TypesDefinition;
import com.palantir.conjure.defs.services.EndpointDefinition.AuthorizationType;
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
        assertThat(mapper.readValue("String", ArgumentDefinition.class))
                .isEqualTo(ArgumentDefinition.of(PrimitiveType.String));
    }

    @Test
    public void testArgumentDefinition_withDocs() throws IOException {
        assertThat(mapper.readValue(multiLineString(
                "type: String",
                "docs: docs"), ArgumentDefinition.class))
                .isEqualTo(ArgumentDefinition.of(PrimitiveType.String, "docs"));
    }

    @Test
    public void testEndpointDefinition_optionals() throws IOException {
        assertThat(mapper.readValue("http: GET /", EndpointDefinition.class))
                .isEqualTo(EndpointDefinition.builder()
                        .http("GET /")
                        .build());
    }

    @Test
    public void testEndpointDefinition_fullySpecified() throws IOException {
        assertThat(mapper.readValue(multiLineString(
                "http: GET /{arg}",
                "authorization: header",
                "args:",
                "  arg: String",
                "returns: String",
                "docs: |",
                "  docs"), EndpointDefinition.class))
                .isEqualTo(EndpointDefinition.builder()
                        .http("GET /{arg}")
                        .authorization(AuthorizationType.HEADER)
                        .args(ImmutableMap.of("arg", ArgumentDefinition.of(PrimitiveType.String)))
                        .returns(PrimitiveType.String)
                        .docs("docs")
                        .build());
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
                                        .putFields("stringField", FieldDefinition.of(PrimitiveType.String))
                                        .build())
                                .build())
                        .build())
                .putServices("TestService", ServiceDefinition.builder()
                        .name("Test Service")
                        .packageName("test.api")
                        .putEndpoints("get", EndpointDefinition.builder()
                                .http("GET /get")
                                .build())
                        .build())
                .build());
    }

    private static String multiLineString(String... lines) {
        return Joiner.on('\n').join(lines);
    }

}
