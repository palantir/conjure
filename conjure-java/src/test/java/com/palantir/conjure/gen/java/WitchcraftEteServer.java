/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.palantir.remoting.api.config.ssl.SslConfiguration;
import com.palantir.remoting3.clients.ClientConfiguration;
import com.palantir.remoting3.clients.ClientConfigurations;
import com.palantir.remoting3.clients.UserAgent;
import com.palantir.remoting3.config.ssl.SslSocketFactories;
import com.palantir.status.health.HealthCheckSharedSecret;
import com.palantir.witchcraft.Witchcraft;
import com.palantir.witchcraft.config.HealthChecksConfiguration;
import com.palantir.witchcraft.config.InstallConfiguration;
import com.palantir.witchcraft.config.KeyStoreConfiguration;
import com.palantir.witchcraft.config.RuntimeConfiguration;
import java.nio.file.Paths;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import org.junit.rules.ExternalResource;

public final class WitchcraftEteServer extends ExternalResource {

    private static final SslConfiguration TRUST_STORE_CONFIGURATION =
            new SslConfiguration.Builder().trustStorePath(Paths.get("var/security/truststore.jks")).build();
    private static final SSLSocketFactory SSL_SOCKET_FACTORY =
            SslSocketFactories.createSslSocketFactory(TRUST_STORE_CONFIGURATION);
    private static final X509TrustManager TRUST_MANAGER =
            SslSocketFactories.createX509TrustManager(TRUST_STORE_CONFIGURATION);

    private final Witchcraft witchcraft = Witchcraft.with(InstallConfiguration.builder()
            .productName("productName")
            .productVersion("productVersion")
            .port(8080)
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

    public ClientConfiguration clientConfiguration() {
        return ClientConfigurations.of(
                ImmutableList.of("https://localhost:8080/witchcraft-example/api"),
                SSL_SOCKET_FACTORY,
                TRUST_MANAGER);
    }

    public UserAgent clientUserAgent() {
        return UserAgent.of(UserAgent.Agent.of("test", "develop"));
    }
}
