/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.names;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.palantir.remoting.api.errors.ErrorType;
import org.junit.Test;

public final class ErrorCodeTest {

    @Test
    public void testValidNames() {
        for (ErrorType.Code code : ErrorType.Code.values()) {
            ErrorCode.of(code.name());
        }
    }

    @Test
    public void testInvalidNames() {
        for (String invalid : new String[] {
                "permissionDenied",
                "PermissionDenied",
                "permission_denied",
                "PERMISSION-DENIED",
                "permission-denied"
                }) {
            assertThatThrownBy(() -> ErrorCode.of(invalid))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(String.format("Invalid error code %s. Must be one of", invalid));
        }
    }

}
