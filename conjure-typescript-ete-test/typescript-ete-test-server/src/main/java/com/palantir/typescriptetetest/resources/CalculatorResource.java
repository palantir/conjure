/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.typescriptetetest.resources;

import com.palantir.tokens.auth.AuthHeader;
import com.palantir.typescriptetetest.api.CalculatorService;
import com.palantir.typescriptetetest.api.IdentitiesResponse;
import com.palantir.typescriptetetest.api.PingResponse;

public final class CalculatorResource implements CalculatorService {

    @Override
    public IdentitiesResponse getIdentities(AuthHeader authHeader) {
        validateToken(authHeader);
        return new IdentitiesResponse(1, 0);
    }

    @Override
    public PingResponse ping(AuthHeader authHeader) {
        return new PingResponse("pong");
    }

    private static void validateToken(AuthHeader authHeader) {
        if (!authHeader.getBearerToken().getToken().equals("token")) {
            throw new RuntimeException("Bad token");
        }
    }
}
