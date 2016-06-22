/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java;

public final class StringCleanup {

    private StringCleanup() {}

    public static String ucfirst(String in) {
        return Character.toUpperCase(in.charAt(0)) + in.substring(1);
    }

    public static String withEndOfLine(String in) {
        if (in.endsWith("\n")) {
            return in;
        }
        return in + "\n";
    }

}
