/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.services;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import java.util.regex.Pattern;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public abstract class ParameterId {

    public static final Pattern HEADER_PATTERN = Pattern.compile("^[A-Z][a-zA-Z0-9]*(-[A-Z][a-zA-Z0-9]*)*$");

    public abstract String name();

    public static ParameterId of(String name) {
        return ImmutableParameterId.builder().name(name).build();
    }

    @Override
    public final String toString() {
        return name();
    }

}
