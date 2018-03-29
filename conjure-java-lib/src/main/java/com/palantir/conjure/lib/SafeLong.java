/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.lib;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.immutables.value.Value;

/**
 * A wrapper around a long which is safe for json-serialization as a number
 * without loss of precision.
 */
@Value.Immutable
public abstract class SafeLong {

    private static final long MIN_SAFE_VALUE = -(1L << 53) + 1;
    private static final long MAX_SAFE_VALUE = (1L << 53) - 1;

    @JsonValue
    @Value.Parameter
    public abstract long longValue();

    @Value.Check
    protected final void check() {
        if (!(MIN_SAFE_VALUE <= longValue() && longValue() <= MAX_SAFE_VALUE)) {
            throw new IllegalArgumentException(String.format(
                    "number must be safely representable in javascript i.e. lie between %s and %s",
                    MIN_SAFE_VALUE, MAX_SAFE_VALUE));
        }
    }

    public static SafeLong valueOf(String value) {
        return SafeLong.of(Long.parseLong(value));
    }

    @JsonCreator
    public static SafeLong of(long value) {
        return ImmutableSafeLong.of(value);
    }

    @Override
    public final String toString() {
        return Long.toString(longValue());
    }

}
