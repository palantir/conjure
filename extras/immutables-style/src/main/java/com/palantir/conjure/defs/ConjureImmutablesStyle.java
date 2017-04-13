/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;

@JsonDeserialize
@Value.Style(
        jdkOnly = true,
        overshadowImplementation = true,
        visibility = ImplementationVisibility.PACKAGE
)
public @interface ConjureImmutablesStyle {

}
