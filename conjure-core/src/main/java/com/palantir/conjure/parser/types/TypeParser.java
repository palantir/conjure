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

import com.palantir.conjure.parser.types.builtin.AnyType;
import com.palantir.conjure.parser.types.builtin.BinaryType;
import com.palantir.conjure.parser.types.builtin.DateTimeType;
import com.palantir.conjure.parser.types.collect.ListType;
import com.palantir.conjure.parser.types.collect.MapType;
import com.palantir.conjure.parser.types.collect.OptionalType;
import com.palantir.conjure.parser.types.collect.SetType;
import com.palantir.conjure.parser.types.names.Namespace;
import com.palantir.conjure.parser.types.names.TypeName;
import com.palantir.conjure.parser.types.reference.ForeignReferenceType;
import com.palantir.conjure.parser.types.reference.LocalReferenceType;
import com.palantir.parsec.ParseException;
import com.palantir.parsec.Parser;
import com.palantir.parsec.ParserState;
import com.palantir.parsec.Parsers;
import com.palantir.parsec.StringParserState;
import com.palantir.parsec.parsers.ExpectationResult;
import com.palantir.parsec.parsers.KeyValueParser;
import com.palantir.parsec.parsers.KeyValueParser.KeyValue;
import com.palantir.parsec.parsers.RawStringParser;

public enum TypeParser implements Parser<ConjureType> {
    INSTANCE;

    public ConjureType parse(String input) throws ParseException {
        ParserState inputParserState = new StringParserState(input);
        ConjureType resultType = Parsers.eof(typeParser()).parse(inputParserState);
        if (resultType == null) {
            throw new ParseException(input, inputParserState);
        }
        return parse(new StringParserState(input));
    }

    @Override
    public ConjureType parse(ParserState input) throws ParseException {
        return typeParser().parse(input);
    }

    private Parser<ConjureType> typeParser() {
        return Parsers.or(
                MapTypeParser.INSTANCE,
                ListTypeParser.INSTANCE,
                SetTypeParser.INSTANCE,
                OptionalTypeParser.INSTANCE,
                TypeFromString.of("any", AnyType.of(), AnyType.class),
                TypeFromString.of("binary", BinaryType.of(), BinaryType.class),
                TypeFromString.of("datetime", DateTimeType.of(), DateTimeType.class),
                ForeignReferenceTypeParser.INSTANCE,
                TypeReferenceParser.INSTANCE);
    }

    private enum TypeReferenceParser implements Parser<LocalReferenceType> {
        INSTANCE;

        public static final Parser<String> REF_PARSER = new RawStringParser(new RawStringParser.AllowableCharacters() {
            @Override
            public boolean isAllowed(char character) {
                return Character.isJavaIdentifierPart(character);
            }

            @Override
            public String getDescription() {
                return "Character is an allowable Java identifier character";
            }
        });

        @Override
        public LocalReferenceType parse(ParserState input) throws ParseException {
            System.out.println("Parsing for type ref");
            input.mark();
            String typeReference = REF_PARSER.parse(input);
            System.out.println(typeReference);
            if (typeReference == null) {
                input.rewind();
                return null;
            }
            TypeName typeName = TypeName.of(typeReference);
            System.out.println("Parsed type name " + typeName);
            input.release();
            return LocalReferenceType.of(typeName);
        }
    }

    // TODO(qchen): Change to ExternalType?
    private enum ForeignReferenceTypeParser implements Parser<ForeignReferenceType> {
        INSTANCE;

        public static final Parser<String> NAMESPACE_PARSER =
                new RawStringParser(new RawStringParser.AllowableCharacters() {
                    @Override
                    public boolean isAllowed(char character) {
                        return '_' == character
                                || ('a' <= character && character <= 'z')
                                || ('A' <= character && character <= 'Z')
                                || ('0' <= character && character <= '9');
                    }

                    @Override
                    public boolean notAllowedToStartWith(char character) {
                        return ('0' <= character && character <= '9');
                    }

                    @Override
                    public String getDescription() {
                        return "Matches ^[_a-zA-Z][_a-zA-Z0-9]*$";
                    }
                });

        @Override
        public ForeignReferenceType parse(ParserState input) throws ParseException {
            String namespace = NAMESPACE_PARSER.parse(input);
            if (Parsers.nullOrUnexpected(Parsers.expect(".").parse(input))) {
                return null;
            }
            String ref = TypeReferenceParser.REF_PARSER.parse(input);
            return ForeignReferenceType.of(Namespace.of(namespace), TypeName.of(ref));
        }
    }

    private enum ListTypeParser implements Parser<ListType> {
        INSTANCE;

        @Override
        public ListType parse(ParserState input) throws ParseException {
            ExpectationResult result = Parsers.expect("list").parse(input);
            if (Parsers.nullOrUnexpected(result)) {
                return null;
            }

            ConjureType itemType =
                    Parsers.liberalBetween("<", TypeParser.INSTANCE, ">").parse(input);
            return ListType.of(itemType);
        }
    }

    private enum SetTypeParser implements Parser<SetType> {
        INSTANCE;

        @Override
        public SetType parse(ParserState input) throws ParseException {
            ExpectationResult result = Parsers.expect("set").parse(input);
            if (Parsers.nullOrUnexpected(result)) {
                return null;
            }

            ConjureType itemType =
                    Parsers.liberalBetween("<", TypeParser.INSTANCE, ">").parse(input);
            return SetType.of(itemType);
        }
    }

    private enum OptionalTypeParser implements Parser<OptionalType> {
        INSTANCE;

        @Override
        public OptionalType parse(ParserState input) throws ParseException {
            ExpectationResult result = Parsers.expect("optional").parse(input);
            if (Parsers.nullOrUnexpected(result)) {
                return null;
            }

            ConjureType itemType =
                    Parsers.liberalBetween("<", TypeParser.INSTANCE, ">").parse(input);
            return OptionalType.of(itemType);
        }
    }

    private enum MapTypeParser implements Parser<MapType> {
        INSTANCE;

        @Override
        public MapType parse(ParserState input) throws ParseException {
            ExpectationResult result = Parsers.expect("map").parse(input);
            if (Parsers.nullOrUnexpected(result)) {
                return null;
            }

            Parser<KeyValue<ConjureType, ConjureType>> kv = Parsers.liberalBetween(
                    "<",
                    new KeyValueParser<>(
                            Parsers.whitespace(TypeParser.INSTANCE),
                            Parsers.whitespace(Parsers.expect(",")),
                            Parsers.whitespace(TypeParser.INSTANCE)),
                    ">");

            KeyValue<ConjureType, ConjureType> types = kv.parse(input);
            return MapType.of(types.getKey(), types.getValue());
        }
    }

    private static final class TypeFromString<T> implements Parser<T> {
        private final String type;
        private final T instance;

        TypeFromString(String type, T instance) {
            this.type = type;
            this.instance = instance;
        }

        @Override
        public T parse(ParserState input) throws ParseException {
            ExpectationResult result = Parsers.expect(type).parse(input);
            if (Parsers.nullOrUnexpected(result)) {
                return null;
            }

            return instance;
        }

        public static <T> TypeFromString<T> of(String type, T instance, Class<T> _metric) {
            return new TypeFromString<>(type, instance);
        }
    }
}
