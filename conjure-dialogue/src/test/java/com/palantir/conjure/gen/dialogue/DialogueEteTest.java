/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.dialogue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import com.google.common.util.concurrent.MoreExecutors;
import com.palantir.dialogue.OkHttpChannel;
import com.palantir.remoting.api.config.ssl.SslConfiguration;
import com.palantir.remoting3.config.ssl.SslSocketFactories;
import com.palantir.tokens.auth.AuthHeader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import test.BlockingTestService;
import test.Complex;
import test.DialogueTestService;

public class DialogueEteTest {

    private static final SslConfiguration SSL_CONFIG = SslConfiguration.of(Paths.get("var/security/truststore.jks"));
    private static final int PORT = 3798;

    @ClassRule
    public static final TemporaryFolder folder = new TemporaryFolder();

    @Rule
    public final WitchcraftEteServer server = new WitchcraftEteServer(PORT);
    private final BlockingTestService client;

    public DialogueEteTest() {
        client = DialogueTestService.blocking(
                createChannel(url("localhost", PORT, "/witchcraft-example/api"), Duration.ofSeconds(1)));
        server.witchcraft().api(new TestResource());
    }

    @Test
    public void returnsData() throws IOException {
        assertThat(client.integer(TestResource.EXPECTED_AUTH)).isEqualTo(TestResource.INT);
        assertThat(client.integerEcho(TestResource.EXPECTED_AUTH, 84)).isEqualTo(84);
        assertThat(client.string(TestResource.EXPECTED_AUTH)).isEqualTo(TestResource.STRING);
        assertThat(client.stringEcho(TestResource.EXPECTED_AUTH, "84")).isEqualTo("84");
        assertThat(client.queryEcho(TestResource.EXPECTED_AUTH, 84)).isEqualTo("84");
        assertThat(client.complex(TestResource.EXPECTED_AUTH))
                .isEqualTo(Complex.of(TestResource.STRING, TestResource.INT));
        assertThat(client.complexEcho(TestResource.EXPECTED_AUTH, Complex.of(TestResource.STRING, TestResource.INT)))
                .isEqualTo(Complex.of(TestResource.STRING, TestResource.INT));
        assertThat(ByteStreams.toByteArray(client.binaryEcho(TestResource.EXPECTED_AUTH, "84")))
                .isEqualTo("\"84\"".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> client.integer(AuthHeader.valueOf("bogus")))
                .isInstanceOf(RuntimeException.class);
    }

    private OkHttpChannel createChannel(URL url, Duration timeout) {
        return OkHttpChannel.of(
                new OkHttpClient.Builder()
                        .protocols(ImmutableList.of(Protocol.HTTP_1_1))
                        // Execute calls on same thread so that async tests are deterministic.
                        .dispatcher(new Dispatcher(MoreExecutors.newDirectExecutorService()))
                        .connectTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                        .readTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                        .writeTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                        .sslSocketFactory(SslSocketFactories.createSslSocketFactory(SSL_CONFIG),
                                SslSocketFactories.createX509TrustManager(SSL_CONFIG))
                        .build(),
                url);
    }

    private static URL url(String host, int port, String path) {
        try {
            return new URL("https", host, port, path);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Failed to create URL", e);
        }
    }
}
