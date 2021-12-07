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

package com.palantir.conjure.parser.types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.palantir.conjure.parser.types.builtin.AnyType;
import com.palantir.conjure.parser.types.builtin.BinaryType;
import com.palantir.conjure.parser.types.collect.ListType;
import com.palantir.conjure.parser.types.collect.MapType;
import com.palantir.conjure.parser.types.collect.OptionalType;
import com.palantir.conjure.parser.types.collect.SetType;
import com.palantir.conjure.parser.types.names.Namespace;
import com.palantir.conjure.parser.types.names.TypeName;
import com.palantir.conjure.parser.types.primitive.PrimitiveType;
import com.palantir.conjure.parser.types.reference.ForeignReferenceType;
import com.palantir.conjure.parser.types.reference.LocalReferenceType;
import com.palantir.parsec.ParseException;
import java.io.IOException;
import org.junit.jupiter.api.Test;

public final class TypeParserTests {

    @Test
    public void testParser_stringType() throws ParseException {
        assertThat(TypeParser.INSTANCE.parse("string")).isEqualTo(PrimitiveType.STRING);
    }

    @Test
    public void testParser_integerType() throws ParseException {
        assertThat(TypeParser.INSTANCE.parse("integer")).isEqualTo(PrimitiveType.INTEGER);
    }

    @Test
    public void testParser_doubleType() throws ParseException {
        assertThat(TypeParser.INSTANCE.parse("double")).isEqualTo(PrimitiveType.DOUBLE);
    }

    @Test
    public void testParser_booleanType() throws ParseException {
        assertThat(TypeParser.INSTANCE.parse("boolean")).isEqualTo(PrimitiveType.BOOLEAN);
    }

    @Test
    public void testParser_safelongType() throws ParseException {
        assertThat(TypeParser.INSTANCE.parse("safelong")).isEqualTo(PrimitiveType.SAFELONG);
    }

    @Test
    public void testParser_ridType() throws ParseException {
        assertThat(TypeParser.INSTANCE.parse("rid")).isEqualTo(PrimitiveType.RID);
    }

    @Test
    public void testParser_bearertokenType() throws ParseException {
        assertThat(TypeParser.INSTANCE.parse("bearertoken")).isEqualTo(PrimitiveType.BEARERTOKEN);
    }

    @Test
    public void testParser_uuidType() throws ParseException {
        assertThat(TypeParser.INSTANCE.parse("uuid")).isEqualTo(PrimitiveType.UUID);
    }

    @Test
    public void testParser_refType() throws ParseException {
        assertThat(TypeParser.INSTANCE.parse("Foo")).isEqualTo(LocalReferenceType.of(TypeName.of("Foo")));
    }

    @Test
    public void testParser_foreignRefType() throws ParseException {
        assertThat(TypeParser.INSTANCE.parse("bar.Foo"))
                .isEqualTo(ForeignReferenceType.of(Namespace.of("bar"), TypeName.of("Foo")));

        assertThat(TypeParser.INSTANCE.parse("bar_1.Foo"))
                .isEqualTo(ForeignReferenceType.of(Namespace.of("bar_1"), TypeName.of("Foo")));

        assertThatThrownBy(() -> TypeParser.INSTANCE.parse("1_bar.Foo")).isInstanceOf(ParseException.class);
    }

    @Test
    public void testParser_anyType() throws ParseException {
        assertThat(TypeParser.INSTANCE.parse("any")).isEqualTo(AnyType.of());
    }

    @Test
    public void testParser_binaryType() throws Exception {
        assertThat(TypeParser.INSTANCE.parse("binary")).isEqualTo(BinaryType.of());
    }

    @Test
    public void testParser_listType() throws ParseException {
        assertThat(TypeParser.INSTANCE.parse("list<string>")).isEqualTo(ListType.of(PrimitiveType.STRING));

        assertThat(TypeParser.INSTANCE.parse("list < string >")).isEqualTo(ListType.of(PrimitiveType.STRING));

        assertThat(TypeParser.INSTANCE.parse("list<list<string>>"))
                .isEqualTo(ListType.of(ListType.of(PrimitiveType.STRING)));
    }

    @Test
    public void testParser_setType() throws ParseException {
        assertThat(TypeParser.INSTANCE.parse("set<string>")).isEqualTo(SetType.of(PrimitiveType.STRING));

        assertThat(TypeParser.INSTANCE.parse("set < string >")).isEqualTo(SetType.of(PrimitiveType.STRING));

        assertThat(TypeParser.INSTANCE.parse("set<list<string>>"))
                .isEqualTo(SetType.of(ListType.of(PrimitiveType.STRING)));
    }

    @Test
    public void testParser_optionalType() throws ParseException {
        assertThat(TypeParser.INSTANCE.parse("optional<string>")).isEqualTo(OptionalType.of(PrimitiveType.STRING));

        assertThat(TypeParser.INSTANCE.parse("optional < string >")).isEqualTo(OptionalType.of(PrimitiveType.STRING));

        assertThat(TypeParser.INSTANCE.parse("optional<list<string>>"))
                .isEqualTo(OptionalType.of(ListType.of(PrimitiveType.STRING)));
    }

    @Test
    public void testParser_mapType() throws ParseException {
        assertThat(TypeParser.INSTANCE.parse("map<string, string>"))
                .isEqualTo(MapType.of(PrimitiveType.STRING, PrimitiveType.STRING));

        assertThat(TypeParser.INSTANCE.parse("map<map<string, string>, optional<string>>"))
                .isEqualTo(MapType.of(
                        MapType.of(PrimitiveType.STRING, PrimitiveType.STRING), OptionalType.of(PrimitiveType.STRING)));
    }

    @Test
    public void testParser_anyMapType() throws ParseException {
        assertThat(TypeParser.INSTANCE.parse("map<string, any>"))
                .isEqualTo(MapType.of(PrimitiveType.STRING, AnyType.of()));
    }

    @Test
    public void testParser_invalidSuffixType() throws ParseException {
        assertThatThrownBy(() -> {
                    TypeParser.INSTANCE.parse("string[]");
                })
                .isInstanceOf(ParseException.class);
    }

    @Test
    public void testParser_invalidType() throws ParseException {
        assertThatThrownBy(() -> {
                    TypeParser.INSTANCE.parse("[]");
                })
                .isInstanceOf(ParseException.class);
    }

    @Test
    public void testDeserializer_stringType() throws IOException {
        assertThat(new ObjectMapper().readValue("\"string\"", ConjureType.class))
                .isEqualTo(PrimitiveType.STRING);
    }

    @Test
    public void testDeserializer_listType() throws IOException {
        assertThat(new ObjectMapper().readValue("\"list<string>\"", ConjureType.class))
                .isEqualTo(ListType.of(PrimitiveType.STRING));
    }

    @Test
    public void testInvalidNames() {
        String invalid = "bytes";
        assertThatThrownBy(() -> TypeParser.INSTANCE.parse(invalid))
                .isInstanceOf(ParseException.class)
                .hasMessage(
                        "TypeNames must be a primitive type [datetime, boolean, string, double, bearertoken, binary,"
                            + " safelong, integer, rid, any, uuid] or match pattern ^[A-Z][a-z0-9]+([A-Z][a-z0-9]+)*$:"
                            + " %s\n"
                            + "at or before character 5\n"
                            + "on or before line 0\n"
                            + "bytes",
                        invalid);
    }
}
