/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.python.poet;

import static com.google.common.base.Preconditions.checkState;

import java.io.PrintStream;

public final class PythonPoetWriter {

    private static final String INDENT = "    ";

    private int indent;
    private PrintStream printStream;

    public PythonPoetWriter(PrintStream printStream) {
        this.indent = 0;
        this.printStream = printStream;
    }

    /**
     * Asserts that the code in runnable leaves the indent unchanged.
     */
    public PythonPoetWriter maintainingIndent(Runnable runnable) {
        int startIndent = indent;
        runnable.run();
        checkState(indent == startIndent, "expected indent to be unchanged");
        return this;
    }

    public PythonPoetWriter decreaseIndent() {
        this.indent--;
        return this;
    }

    public PythonPoetWriter increaseIndent() {
        this.indent++;
        return this;
    }

    public PythonPoetWriter write(String content) {
        printStream.print(content);
        return this;
    }

    public PythonPoetWriter writeLine() {
        printStream.print("\n");
        return this;
    }

    public PythonPoetWriter writeLine(String content) {
        printStream.print(content);
        return writeLine();
    }

    public PythonPoetWriter writeIndented() {
        for (int i = 0; i < indent; i++) {
            printStream.print(INDENT);
        }
        return this;
    }

    public PythonPoetWriter writeIndented(String content) {
        writeIndented();
        write(content);
        return this;
    }

    public PythonPoetWriter writeIndentedLine(String content) {
        writeIndented(content);
        writeLine();
        return this;
    }

    public PythonPoetWriter writeIndentedLine(String formatString, Object... args) {
        return writeIndentedLine(String.format(formatString, args));
    }

    public PythonPoetWriter emit(Emittable emittable) {
        emittable.emit(this);
        return this;
    }
}
