/*
 * (c) Copyright 2021 Palantir Technologies Inc. All rights reserved.
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

import com.palantir.conjure.parser.types.names.TypeName;
import com.palantir.conjure.parser.types.reference.LocalReferenceError;
import com.palantir.parsec.ParseException;
import com.palantir.parsec.Parser;
import com.palantir.parsec.ParserState;
import com.palantir.parsec.Parsers;
import com.palantir.parsec.StringParserState;
import com.palantir.parsec.parsers.RawStringParser;

public enum ErrorParser implements Parser<ConjureError> {
    INSTANCE;

    public ConjureError parse(String input) throws ParseException {
        ParserState inputParserState = new StringParserState(input);
        ConjureError resultType = Parsers.eof(ErrorParser.INSTANCE).parse(inputParserState);
        if (resultType == null) {
            throw new ParseException(input, inputParserState);
        }
        return resultType;
    }

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
    public ConjureError parse(ParserState input) throws ParseException {
        input.mark();
        String errorReference = REF_PARSER.parse(input);
        if (errorReference == null) {
            input.rewind();
            return null;
        }
        TypeName typeName;
        try {
            typeName = TypeName.of(errorReference);
        } catch (IllegalArgumentException _e) {
            input.rewind();
            return null;
        }
        input.release();
        return LocalReferenceError.of(typeName);
    }
}
