/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.parsec.parsers;

import com.palantir.parsec.ParseException;
import com.palantir.parsec.Parser;
import com.palantir.parsec.ParserState;
import com.palantir.parsec.Parsers;

public final class BooleanParser implements Parser<Boolean> {

    @Override
    public Boolean parse(ParserState input) throws ParseException {
        String val = new RawStringParser().parse(input);
        return !Parsers.nullOrEmpty(val)
                && (val.equalsIgnoreCase("true") || val.equalsIgnoreCase("yes") || val.equalsIgnoreCase("1"));
    }

}
