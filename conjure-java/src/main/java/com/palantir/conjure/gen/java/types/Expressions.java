/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import com.google.common.base.Joiner;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class Expressions {
    private Expressions() {}

    public static CodeBlock requireNonNull(String fieldName, String message) {
        return CodeBlock.of("$1T.requireNonNull($2N, $3S)", Objects.class, fieldName, message);
    }

    public static CodeBlock constructorCall(ClassName clazz, Collection<?> args) {
        return CodeBlock.of("$1T(" + indexParams(2, args.size() + 2) + ")", append(clazz, args));
    }

    public static CodeBlock localMethodCall(String method, Collection<?> args) {
        return CodeBlock.of("$1N(" + indexParams(2, args.size() + 2) + ")", append(method, args));
    }

    public static CodeBlock objectArray(Collection<?> args) {
        return CodeBlock.of("new Object[]{" + indexParams(1, args.size() + 1) + "}", args.toArray());
    }

    private static Object[] append(Object one, Collection<?> rest) {
        return Stream.concat(Stream.of(one), rest.stream()).toArray();
    }

    private static String indexParams(int lower, int upper) {
        return Joiner.on(", ").join(indexStringInRange("$%dN", lower, upper));
    }

    private static Iterator<String> indexStringInRange(String format, int lower, int upper) {
        return IntStream.range(lower, upper).mapToObj(i -> String.format(format, i)).iterator();
    }
}
