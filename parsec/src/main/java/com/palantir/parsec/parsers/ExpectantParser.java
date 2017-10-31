/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.parsec.parsers;

import com.palantir.parsec.Parser;
import com.palantir.parsec.ParserState;

public final class ExpectantParser implements Parser<ExpectationResult> {

    private final String expectation;

    public ExpectantParser(String expectation) {
        this.expectation = expectation;
    }

    @Override
    public ExpectationResult parse(ParserState input) {
        StringBuilder sb = new StringBuilder();
        int curr = input.curr();
        while (curr != -1 && sb.length() < expectation.length()) {
            sb.append((char) curr);
            curr = input.next();
        }

        if (sb.toString().equals(expectation)) {
            return ExpectationResult.CORRECT;
        } else {
            return ExpectationResult.INCORRECT;
        }
    }

}
