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

package com.palantir.parsec.parsers;

import com.palantir.parsec.ParseException;
import com.palantir.parsec.Parser;
import com.palantir.parsec.ParserState;
import com.palantir.parsec.Parsers;
import com.palantir.parsec.parsers.KeyValueParser.KeyValue;

public final class KeyValueParser<A, B> implements Parser<KeyValue<A, B>> {

    private Parser<A> keyParser;
    private Parser<B> valueParser;
    private Parser<?> separator;

    public KeyValueParser(Parser<A> keyParser, Parser<?> separator, Parser<B> valueParser) {
        this.keyParser = keyParser;
        this.separator = separator;
        this.valueParser = valueParser;
    }

    @Override
    public KeyValue<A, B> parse(ParserState input) throws ParseException {
        A key = Parsers.gingerly(keyParser).parse(input);
        if (!Parsers.nullOrUnexpected(key)) {
            if (!Parsers.nullOrUnexpected(Parsers.gingerly(separator).parse(input))) {
                B val = Parsers.gingerly(valueParser).parse(input);
                if (!Parsers.nullOrUnexpected(val)) {
                    return new KeyValue<>(key, val);
                } else {
                    throw new ParseException("Found key '" + key + "' without associated value.", input);
                }
            }
        }
        return null;
    }

    public static final class KeyValue<A, B> {
        private final A key;
        private final B value;

        public KeyValue(A key, B value) {
            this.key = key;
            this.value = value;
        }

        public A getKey() {
            return key;
        }

        public B getValue() {
            return value;
        }
    }
}
