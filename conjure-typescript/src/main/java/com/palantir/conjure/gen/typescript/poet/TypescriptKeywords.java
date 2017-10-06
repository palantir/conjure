/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.poet;

import com.google.common.collect.ImmutableSet;

/**
 * Keywords taken from https://github.com/Microsoft/TypeScript/issues/2536.
 */
public final class TypescriptKeywords {
    private static final ImmutableSet<String> strictModeReservedKeywords = ImmutableSet.of(
            "as",
            "implements",
            "interface",
            "let",
            "package",
            "private",
            "protected",
            "public",
            "static",
            "yield");
    private static final ImmutableSet<String> reservedKeywords = ImmutableSet.of(
            "break",
            "case",
            "catch",
            "class",
            "const",
            "continue",
            "debugger",
            "default",
            "delete",
            "do",
            "else",
            "enum",
            "export",
            "extends",
            "false",
            "finally",
            "for",
            "function",
            "if",
            "import",
            "in",
            "instanceof",
            "new",
            "null",
            "return",
            "super",
            "switch",
            "this",
            "throw",
            "true",
            "try",
            "typeof",
            "var",
            "void",
            "while",
            "with");
    private static final ImmutableSet<String> contextualKeywords = ImmutableSet.of(
            "any",
            "boolean",
            "constructor",
            "declare",
            "get",
            "module",
            "require",
            "number",
            "set",
            "string",
            "symbol",
            "type",
            "from",
            "of");

    private TypescriptKeywords() {}

    public static boolean isKeyword(String value) {
        return strictModeReservedKeywords.contains(value)
                || reservedKeywords.contains(value)
                || contextualKeywords.contains(value);
    }

}
