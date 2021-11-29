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

package com.palantir.parsec;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class StringParserState implements ParserState {
    private static final int SNIPPET_WIDTH = 60;
    private static final int SNIPPET_HEIGHT = 5;

    private final CharSequence seq;
    private final Deque<Integer> marks = new ArrayDeque<>();
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

    @Override
    public String fetchSnippetForException() {
        int colStart = Math.max(0, getCharPosition() - SNIPPET_WIDTH / 2);
        int colEnd = colStart + SNIPPET_WIDTH;

        String[] lines = seq.toString().split("\n", -1);

        int rowStart = Math.max(0, getLine() - SNIPPET_HEIGHT / 2);
        int rowEnd = Math.min(lines.length, rowStart + SNIPPET_HEIGHT);

        String prefix = colStart > 0 ? "... " : "";

        String indicatorLineSuffix = IntStream.range(0, getCharPosition() - colStart + prefix.length())
                        .mapToObj(_i -> " ")
                        .collect(Collectors.joining())
                + "^";

        return IntStream.range(rowStart, rowEnd)
                .mapToObj(rowNumber -> {
                    String line = lines[rowNumber];
                    String suffix = (colEnd < line.length() ? " ..." : "")
                            + (rowNumber == getLine() ? "\n" + indicatorLineSuffix : "");
                    String segment = line.substring(Math.min(colStart, line.length()), Math.min(colEnd, line.length()));
                    return prefix + segment + suffix;
                })
                .collect(Collectors.joining("\n"));
    }
}
