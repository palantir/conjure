/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.parsec;

import java.util.Deque;
import java.util.LinkedList;

public final class StringParserState implements ParserState {

    private final CharSequence seq;
    private final Deque<Integer> marks = new LinkedList<Integer>();
    private int current = 0;

    public StringParserState(CharSequence str) {
        this.seq = str;
    }

    @Override
    public int curr() {
        return current < seq.length() ? seq.charAt(current) : -1;
    }

    @Override
    public int next() {
        // not sure this matters, but we'll enforce that current
        // never exceeds actual length by more than 1
        current = (++current <= seq.length()) ? current : seq.length();
        return curr();
    }

    @Override
    public void mark() {
        marks.push(current);
    }

    @Override
    public void rewind() {
        current = marks.pop();
    }

    @Override
    public void release() {
        marks.pop();
    }

    @Override
    public int getLine() {
        return 0;
    }

    @Override
    public int getCharPosition() {
        return current;
    }

}
