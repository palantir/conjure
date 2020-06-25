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
import java.util.ArrayList;
import java.util.List;

public final class ListParser<T> implements Parser<List<T>> {

    private final Parser<?> separator;
    private final Parser<T> valueParser;

    public ListParser(Parser<T> valueParser, Parser<?> separator) {
        this.valueParser = valueParser;
        this.separator = separator;
    }

    @Override
    public List<T> parse(ParserState input) throws ParseException {
        List<T> results = new ArrayList<>();
        do {
            T val = Parsers.gingerly(valueParser).parse(input);
            if (!Parsers.nullOrUnexpected(val)) {
                results.add(val);
            } else {
                break;
            }
        } while (Parsers.gingerly(separator).parse(input) != null);
        return results;
    }
}
