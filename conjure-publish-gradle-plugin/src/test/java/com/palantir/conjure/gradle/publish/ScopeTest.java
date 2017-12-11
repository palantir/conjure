/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gradle.publish;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.Test;

public class ScopeTest {

    @Test
    public void normalizes() throws Exception {
        assertThat(Scope.of("@foundry")).isEqualTo(Scope.of("foundry"));
    }

    @Test
    public void bad() throws Exception {
        assertThatThrownBy(() -> Scope.of("bad/bad")).hasMessage("Scope must not contain '/': [bad/bad]");
    }
}
