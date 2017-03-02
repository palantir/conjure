/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.lib;

import static com.google.common.base.Preconditions.checkState;

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
        checkState(MIN_SAFE_VALUE <= longValue() && longValue() <= MAX_SAFE_VALUE,
                "number must be safely representable in javascript i.e. lie between %s and %s",
                MIN_SAFE_VALUE, MAX_SAFE_VALUE);
    }

    @JsonCreator
    public static SafeLong of(long value) {
        return ImmutableSafeLong.of(value);
    }

}
