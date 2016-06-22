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
        assertThat(TypeParser.INSTANCE.parse("string"))
                .isEqualTo(ReferenceType.of("string"));
    }

    @Test
    public void testParser_listType() throws ParseException {
        assertThat(TypeParser.INSTANCE.parse("list<string>"))
             .isEqualTo(ListType.of(PrimitiveType.STRING));

        assertThat(TypeParser.INSTANCE.parse("list < string >"))
                .isEqualTo(ListType.of(PrimitiveType.STRING));

        assertThat(TypeParser.INSTANCE.parse("list<list<string>>"))
                .isEqualTo(ListType.of(ListType.of(PrimitiveType.STRING)));
    }

    @Test
    public void testParser_setType() throws ParseException {
        assertThat(TypeParser.INSTANCE.parse("set<string>"))
             .isEqualTo(SetType.of(PrimitiveType.STRING));

        assertThat(TypeParser.INSTANCE.parse("set < string >"))
                .isEqualTo(SetType.of(PrimitiveType.STRING));

        assertThat(TypeParser.INSTANCE.parse("set<list<string>>"))
                .isEqualTo(SetType.of(ListType.of(PrimitiveType.STRING)));
    }

    @Test
    public void testParser_optionalType() throws ParseException {
        assertThat(TypeParser.INSTANCE.parse("optional<string>"))
                .isEqualTo(OptionalType.of(PrimitiveType.STRING));

        assertThat(TypeParser.INSTANCE.parse("optional < string >"))
                .isEqualTo(OptionalType.of(PrimitiveType.STRING));

        assertThat(TypeParser.INSTANCE.parse("optional<list<string>>"))
                .isEqualTo(OptionalType.of(ListType.of(PrimitiveType.STRING)));
    }

    @Test
    public void testParser_mapType() throws ParseException {
        assertThat(TypeParser.INSTANCE.parse("map<string, string>"))
                .isEqualTo(MapType.of(
                        PrimitiveType.STRING,
                        PrimitiveType.STRING));

        assertThat(TypeParser.INSTANCE.parse("map<map<string, string>, optional<string>>"))
                .isEqualTo(
                        MapType.of(
                                MapType.of(
                                        PrimitiveType.STRING,
                                        PrimitiveType.STRING),
                                OptionalType.of(PrimitiveType.STRING)));
    }

    @Test
    public void testDeserializer_rawType() throws IOException {
        assertThat(new ObjectMapper().readValue("\"string\"", ConjureType.class))
                .isEqualTo(PrimitiveType.STRING);
    }

    @Test
    public void testDeserializer_listType() throws IOException {
        assertThat(new ObjectMapper().readValue("\"list<string>\"", ConjureType.class))
                .isEqualTo(ListType.of(PrimitiveType.STRING));
    }

}
