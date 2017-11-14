/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.poet;

import java.io.PrintStream;
import java.util.List;

public final class TypescriptPoetWriter {

    private static final String INDENT = "    ";

    private int indent;
    private PrintStream printStream;

    public TypescriptPoetWriter(PrintStream printStream) {
        this.indent = 0;
        this.printStream = printStream;
    }

    public TypescriptPoetWriter decreaseIndent() {
        this.indent--;
        return this;
    }

    public TypescriptPoetWriter increaseIndent() {
        this.indent++;
        return this;
    }

    public TypescriptPoetWriter write(String content) {
        printStream.print(content);
        return this;
    }

    public TypescriptPoetWriter writeLine() {
        printStream.print("\n");
        return this;
    }

    public TypescriptPoetWriter writeLine(String content) {
        printStream.print(content);
        return writeLine();
    }

    public TypescriptPoetWriter writeIndented() {
        for (int i = 0; i < indent; i++) {
            printStream.print(INDENT);
        }
        return this;
    }

    public TypescriptPoetWriter writeIndented(String content) {
        writeIndented();
        write(content);
        return this;
    }

    public TypescriptPoetWriter writeIndentedLine(String content) {
        writeIndented(content);
        writeLine();
        return this;
    }

    public TypescriptPoetWriter emit(Emittable emittable) {
        emittable.emit(this);
        return this;
    }

    public TypescriptPoetWriter emitIndented(Emittable emittable) {
        writeIndented();
        emittable.emit(this);
        return this;
    }

    public TypescriptPoetWriter emitJoin(List<? extends Emittable> emittables, String joinStr) {
        if (emittables.size() > 0) {
            emittables.get(0).emit(this);
            emittables.subList(1, emittables.size()).forEach(emittable -> {
                write(joinStr);
                emittable.emit(this);
            });
        }
        return this;
    }
}
