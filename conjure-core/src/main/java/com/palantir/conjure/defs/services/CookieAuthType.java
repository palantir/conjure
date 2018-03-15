/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.services;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface CookieAuthType extends AuthType {
    String cookieName();

    static CookieAuthType cookie(String cookieName) {
        return ImmutableCookieAuthType.builder().cookieName(cookieName).build();
    }
}
