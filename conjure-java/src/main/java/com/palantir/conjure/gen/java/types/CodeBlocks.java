/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import com.squareup.javapoet.CodeBlock;
import java.util.Arrays;

public final class CodeBlocks {

    private CodeBlocks() {}

    public static CodeBlock statement(String format, Object... args) {
        return CodeBlock.builder().addStatement(format, args).build();
    }

    public static CodeBlock of(CodeBlock... blocks) {
        return of(Arrays.asList(blocks));
    }

    public static CodeBlock of(Iterable<CodeBlock> blocks) {
        CodeBlock.Builder builder = CodeBlock.builder();
        for (CodeBlock block : blocks) {
            builder.add(block);
        }
        return builder.build();
    }

}
