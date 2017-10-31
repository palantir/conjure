/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import com.palantir.conjure.defs.types.builtin.AnyType;
import com.palantir.conjure.defs.types.builtin.BinaryType;
import com.palantir.conjure.defs.types.builtin.DateTimeType;
import com.palantir.conjure.defs.types.collect.ListType;
import com.palantir.conjure.defs.types.collect.MapType;
import com.palantir.conjure.defs.types.collect.OptionalType;
import com.palantir.conjure.defs.types.collect.SetType;
import com.palantir.conjure.defs.types.names.Namespace;
import com.palantir.conjure.defs.types.names.TypeName;
import com.palantir.conjure.defs.types.reference.ForeignReferenceType;
import com.palantir.conjure.defs.types.reference.LocalReferenceType;
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
        return parse(new StringParserState(input));
    }

    @Override
    public ConjureType parse(ParserState input) throws ParseException {
        Parser<ConjureType> parser = Parsers.or(
                MapTypeParser.INSTANCE,
                ListTypeParser.INSTANCE,
                SetTypeParser.INSTANCE,
                OptionalTypeParser.INSTANCE,
                TypeFromString.of("any", AnyType.of()),
                TypeFromString.of("binary", BinaryType.of()),
                TypeFromString.of("datetime", DateTimeType.of()),
                ForeignReferenceTypeParser.INSTANCE,
                TypeReferenceParser.INSTANCE);

        return parser.parse(input);
    }

    private enum TypeReferenceParser implements Parser<LocalReferenceType> {
        INSTANCE;

        public static final Parser<String> REF_PARSER = new RawStringParser(
                new RawStringParser.AllowableCharacters() {
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
            return LocalReferenceType.of(TypeName.of(REF_PARSER.parse(input)));
        }
    }

    private enum ForeignReferenceTypeParser implements Parser<ForeignReferenceType> {
        INSTANCE;

        public static final Parser<String> NAMESPACE_PARSER = new RawStringParser(
                new RawStringParser.AllowableCharacters() {
                    @Override
                    public boolean isAllowed(char character) {
                        return ('a' <= character && character <= 'z')
                                || ('A' <= character && character <= 'Z');
                    }

                    @Override
                    public String getDescription() {
                        return "Character is one of [a-zA-Z]";
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

            ConjureType itemType = Parsers.liberalBetween("<", TypeParser.INSTANCE, ">").parse(input);
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

            ConjureType itemType = Parsers.liberalBetween("<", TypeParser.INSTANCE, ">").parse(input);
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

            ConjureType itemType = Parsers.liberalBetween("<", TypeParser.INSTANCE, ">").parse(input);
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

        public static <T> TypeFromString<T> of(String type, T instance) {
            return new TypeFromString<>(type, instance);
        }
    }
}
