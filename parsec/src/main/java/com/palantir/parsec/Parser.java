/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.parsec;

public interface Parser<T> {

    T parse(ParserState input) throws ParseException;

}
