/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.parsec;

public interface ParserState {

    /**
     * Current character in the stream.
     *
     * @return current character in the stream, of if unitialized, the first character
     */
    int curr();

    /**
     * Increments the character stream by one.
     *
     * @return current character in the stream
     */
    int next();

    /**
     * Mark beginning of an interesting feature (held as a stack).
     */
    void mark();

    /**
     * Pop the last mark.
     */
    void release();

    /**
     * Pop the last mark and return to its position.
     */
    void rewind();

    int getLine();

    int getCharPosition();

}
