/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java;

import com.palantir.conjure.lib.SafeLong;
import com.palantir.product.EteService;
import com.palantir.ri.ResourceIdentifier;
import com.palantir.tokens.auth.AuthHeader;
import com.palantir.tokens.auth.BearerToken;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;
import javax.ws.rs.core.StreamingOutput;

public final class EteResource implements EteService {
    @Override
    public String string(AuthHeader authHeader) {
        return "Hello, world!";
    }

    @Override
    public int integer(AuthHeader authHeader) {
        return 1234;
    }

    @Override
    public double double_(AuthHeader authHeader) {
        return 1 / 3d;
    }

    @Override
    public boolean boolean_(AuthHeader authHeader) {
        return true;
    }

    @Override
    public SafeLong safelong(AuthHeader authHeader) {
        return SafeLong.of(12345L);
    }

    @Override
    public ResourceIdentifier rid(AuthHeader authHeader) {
        return ResourceIdentifier.of("ri.foundry.main.dataset.1234");
    }

    @Override
    public BearerToken bearertoken(AuthHeader authHeader) {
        return BearerToken.valueOf("fake");
    }

    @Override
    public Optional<String> optionalString(AuthHeader authHeader) {
        return Optional.of("foo");
    }

    @Override
    public Optional<String> optionalEmpty(AuthHeader authHeader) {
        return Optional.empty();
    }

    @Override
    public ZonedDateTime datetime(AuthHeader authHeader) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(1234L), ZoneId.from(ZoneOffset.UTC));
    }

    @Override
    public StreamingOutput binary(AuthHeader authHeader) {
        return (outputStream) -> outputStream.write("Hello, world!".getBytes(StandardCharsets.UTF_8));
    }
}
