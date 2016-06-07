/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import static com.google.common.truth.Truth.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.palantir.parsec.ParseException;
import java.io.IOException;
import org.junit.Test;

public final class ConjureTypeParserTests {

    @Test
    public void testParser_refType() throws ParseException {
        assertThat(TypeParser.INSTANCE.parse("String"))
                .isEqualTo(ReferenceType.of("String"));
    }

    @Test
    public void testParser_listType() throws ParseException {
        assertThat(TypeParser.INSTANCE.parse("List<String>"))
             .isEqualTo(ListType.of(PrimitiveType.String));

        assertThat(TypeParser.INSTANCE.parse("List < String >"))
                .isEqualTo(ListType.of(PrimitiveType.String));

        assertThat(TypeParser.INSTANCE.parse("List<List<String>>"))
                .isEqualTo(ListType.of(ListType.of(PrimitiveType.String)));
    }

    @Test
    public void testParser_setType() throws ParseException {
        assertThat(TypeParser.INSTANCE.parse("Set<String>"))
             .isEqualTo(SetType.of(PrimitiveType.String));

        assertThat(TypeParser.INSTANCE.parse("Set < String >"))
                .isEqualTo(SetType.of(PrimitiveType.String));

        assertThat(TypeParser.INSTANCE.parse("Set<List<String>>"))
                .isEqualTo(SetType.of(ListType.of(PrimitiveType.String)));
    }

    @Test
    public void testParser_optionalType() throws ParseException {
        assertThat(TypeParser.INSTANCE.parse("Optional<String>"))
                .isEqualTo(OptionalType.of(PrimitiveType.String));

        assertThat(TypeParser.INSTANCE.parse("Optional < String >"))
                .isEqualTo(OptionalType.of(PrimitiveType.String));

        assertThat(TypeParser.INSTANCE.parse("Optional<List<String>>"))
                .isEqualTo(OptionalType.of(ListType.of(PrimitiveType.String)));
    }

    @Test
    public void testParser_mapType() throws ParseException {
        assertThat(TypeParser.INSTANCE.parse("Map<String, String>"))
                .isEqualTo(MapType.of(
                        PrimitiveType.String,
                        PrimitiveType.String));

        assertThat(TypeParser.INSTANCE.parse("Map<Map<String, String>, Optional<String>>"))
                .isEqualTo(
                        MapType.of(
                                MapType.of(
                                        PrimitiveType.String,
                                        PrimitiveType.String),
                                OptionalType.of(PrimitiveType.String)));
    }

    @Test
    public void testDeserializer_rawType() throws IOException {
        assertThat(new ObjectMapper().readValue("\"String\"", ConjureType.class))
                .isEqualTo(PrimitiveType.String);
    }

    @Test
    public void testDeserializer_listType() throws IOException {
        assertThat(new ObjectMapper().readValue("\"List<String>\"", ConjureType.class))
                .isEqualTo(ListType.of(PrimitiveType.String));
    }

}
