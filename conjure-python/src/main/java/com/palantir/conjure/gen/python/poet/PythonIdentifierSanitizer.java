/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.python.poet;

import com.google.common.collect.ImmutableSet;

public final class PythonIdentifierSanitizer {

    private static final ImmutableSet<String> pythonKeywords = ImmutableSet.of(
            "and",
            "as",
            "assert",
            "break",
            "class",
            "continue",
            "def",
            "del",
            "elif",
            "else",
            "except",
            "exec",
            "finally",
            "for",
            "from",
            "global",
            "if",
            "import",
            "in",
            "is",
            "lambda",
            "not",
            "or",
            "pass",
            "print",
            "raise",
            "return",
            "try",
            "while",
            "with",
            "yield");

    /**
     * If the identifier is a python keyword, prepends "_".
     * <p>
     * Does no case conversion.
     */
    public static String sanitize(String identifier) {
        return isKeyword(identifier) ? identifier + "_" : identifier;
    }

    public static boolean isKeyword(String identifier) {
        return pythonKeywords.contains(identifier);
    }

    private PythonIdentifierSanitizer() {}

}
