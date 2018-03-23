/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.dialogue;

import com.google.common.base.Preconditions;
import com.palantir.tokens.auth.AuthHeader;
import java.nio.charset.StandardCharsets;
import javax.ws.rs.core.StreamingOutput;
import test.Complex;
import test.TestService;

public final class TestResource implements TestService {
    public static final int INT = 42;
    public static final String STRING = "42";
    public static final AuthHeader EXPECTED_AUTH = AuthHeader.valueOf("foo");

    @Override
    public String string(AuthHeader authHeader) {
        Preconditions.checkArgument(authHeader.equals(EXPECTED_AUTH));
        return STRING;
    }

    @Override
    public String stringEcho(AuthHeader authHeader, String string) {
        Preconditions.checkArgument(authHeader.equals(EXPECTED_AUTH));
        return string;
    }

    @Override
    public int integer(AuthHeader authHeader) {
        Preconditions.checkArgument(authHeader.equals(EXPECTED_AUTH));
        return INT;
    }

    @Override
    public int integerEcho(AuthHeader authHeader, int integer) {
        Preconditions.checkArgument(authHeader.equals(EXPECTED_AUTH));
        return integer;
    }

    @Override
    public String queryEcho(AuthHeader authHeader, int integer) {
        return Integer.toString(integer);
    }

    @Override
    public Complex complex(AuthHeader authHeader) {
        Preconditions.checkArgument(authHeader.equals(EXPECTED_AUTH));
        return Complex.of(STRING, INT);
    }

    @Override
    public Complex complexEcho(AuthHeader authHeader, Complex complex) {
        Preconditions.checkArgument(authHeader.equals(EXPECTED_AUTH));
        return complex;
    }

    @Override
    public StreamingOutput binaryEcho(AuthHeader authHeader, String string) {
        return (outputStream) -> outputStream.write(string.getBytes(StandardCharsets.UTF_8));
    }
}
