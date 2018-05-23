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

import com.google.common.collect.Maps;
import com.palantir.parsec.ParseException;
import com.palantir.parsec.Parser;
import com.palantir.parsec.ParserState;
import com.palantir.parsec.Parsers;
import java.util.Map;

public final class MapParser<A, B> implements Parser<Map<A, B>> {

    private final Parser<A> keyParser;
    private final Parser<B> valueParser;
    private final Parser<?> separator;

    public MapParser(Parser<A> keyParser, Parser<B> valueParser, Parser<?> separator) {
        this.keyParser = keyParser;
        this.valueParser = valueParser;
        this.separator = separator;
    }

    @Override
    public Map<A, B> parse(ParserState input) throws ParseException {
        Map<A, B> results = Maps.newHashMap();
        do {
            A key = Parsers.gingerly(keyParser).parse(input);
            if (!Parsers.nullOrUnexpected(key)) {
                B val = Parsers.gingerly(valueParser).parse(input);
                if (!Parsers.nullOrUnexpected(val)) {
                    results.put(key, val);
                } else {
                    throw new ParseException("Found key '" + key + "' without associated value.", input);
                }
            } else {
                break;
            }
        } while (Parsers.gingerly(separator).parse(input) != null);
        return results;
    }
}
