/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.parsec;

public final class ParseException extends Exception {

    private static final long serialVersionUID = 8326653219913481816L;
    private final String message;
    private final ParserState state;

    public ParseException(String message, ParserState state) {
        this.message = message;
        this.state = state;
    }

    @Override
    public String getMessage() {

        // sb is the next 100 characters of the text being parsed
        StringBuilder sb = new StringBuilder();
        int curr;
        int counter = 0;
        curr = state.curr();
        while (-1 != curr && ++counter < 100) {
            sb.append((char) curr);
            curr = state.next();
        }

        String charInfo = "at or before character " + state.getCharPosition();
        String lineInfo = "on or before line " + state.getLine();

        return message + "\n" + charInfo + "\n" + lineInfo + "\n" + sb.toString();
    }

    public ParserState getState() {
        return state;
    }

}
