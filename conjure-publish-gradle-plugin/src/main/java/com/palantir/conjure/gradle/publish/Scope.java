/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gradle.publish;

import com.google.common.base.Preconditions;
import org.immutables.value.Value;

@Value.Immutable
@SuppressWarnings("checkstyle:designforextension")
public abstract class Scope {

    @Value.Parameter
    protected abstract String string();

    @Value.Check
    protected Scope check() {
        if (string().startsWith("@")) {
            return Scope.of(string().substring(1));
        }

        Preconditions.checkState(
                !string().contains("/"),
                "Scope must not contain '/':", string());

        return this;
    }

    public static Scope of(String string) {
        return ImmutableScope.of(string);
    }

    @Override
    public final String toString() {
        return string();
    }
}
