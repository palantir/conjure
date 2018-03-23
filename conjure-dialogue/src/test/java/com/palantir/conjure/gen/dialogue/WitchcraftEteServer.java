/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.dialogue;

import com.google.common.base.Strings;
import com.palantir.status.health.HealthCheckSharedSecret;
import com.palantir.witchcraft.Witchcraft;
import com.palantir.witchcraft.config.HealthChecksConfiguration;
import com.palantir.witchcraft.config.InstallConfiguration;
import com.palantir.witchcraft.config.KeyStoreConfiguration;
import com.palantir.witchcraft.config.RuntimeConfiguration;
import org.junit.rules.ExternalResource;

public final class WitchcraftEteServer extends ExternalResource {

    private final Witchcraft witchcraft;

    WitchcraftEteServer(int port) {
        witchcraft = Witchcraft.with(InstallConfiguration.builder()
                .productName("productName")
                .productVersion("productVersion")
                .port(port)
                .contextPath("/witchcraft-example")
                .keystore(KeyStoreConfiguration.builder()
                        .keyAlias("witchcraft-example.palantir.pt-cert")
                        .password("keystore")
                        .path("var/security/keystore.jks")
                        .build())
                .build(), RuntimeConfiguration.builder()
                .healthChecks(HealthChecksConfiguration.builder()
                        .sharedSecret(HealthCheckSharedSecret.valueOf(Strings.repeat("s", 16)))
                        .build())
                .build());
    }

    @Override
    protected void before() {
        witchcraft.start();
    }

    @Override
    protected void after() {
        witchcraft.stop();
    }

    public Witchcraft witchcraft() {
        return witchcraft;
    }
}
