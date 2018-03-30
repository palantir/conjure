/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.testing;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.palantir.conjure.testing.reference.SerializationService;
import com.palantir.remoting3.clients.UserAgent;
import com.palantir.remoting3.ext.jackson.ObjectMappers;
import com.palantir.remoting3.okhttp.OkHttpClients;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.Rule;
import org.junit.Test;

/**
 * Verifies that the reference server implements the Conjure spec. This test must not use Conjure objects or types,
 * or Conjure clients like JaxRsClients, RetrofitClients, Dialogue clients, etc.
 */
public class WitchcraftReferenceServerTest extends TestBase {

    private static final ObjectMapper mapper = ObjectMappers.newClientObjectMapper();

    @Rule
    public final WitchcraftReferenceServer server = new WitchcraftReferenceServer();
    private final OkHttpClient okhttpClient;

    public WitchcraftReferenceServerTest() {
        okhttpClient = OkHttpClients.create(
                server.clientConfiguration(),
                UserAgent.of(UserAgent.Agent.of("test", "0.0.0")),
                SerializationService.class);

        server.witchcraft().servletHolder(new ServletHolder(new SerializationServlet()), "/serialization/*");
    }


    @Test
    public void primitives() {
        assertThat(fetch("/string", "\"42\"")).isEqualTo("\"42\"");
        assertThat(fetch("/integer", "42")).isEqualTo("42");
        assertThat(fetch("/integer", "-42")).isEqualTo("-42");
        assertThat(fetch("/integer", "0")).isEqualTo("0");
        assertThat(fetch("/double", "4.2")).isEqualTo("4.2");
        assertThat(fetch("/double", "4.0")).isEqualTo("4.0");
        assertThat(fetch("/safelong", "42")).isEqualTo("42");
        assertThat(fetch("/safelong", "-42")).isEqualTo("-42");
        assertThat(fetch("/safelong", "0")).isEqualTo("0");
        assertThat(fetch("/rid", "\"ri.foo.bar.baz.boom\"")).isEqualTo("\"ri.foo.bar.baz.boom\"");
        assertThat(fetch("/bearertoken", "\"foo-bar\"")).isEqualTo("\"foo-bar\"");
        assertThat(fetch("/uuid", "\"123e4567-e89b-12d3-a456-426655440000\""))
                .isEqualTo("\"123e4567-e89b-12d3-a456-426655440000\"");
        assertThat(fetch("/datetime", "\"2017-01-02T03:04:05Z\"")).isEqualTo("\"2017-01-02T03:04:05Z\"");
        assertThat(fetch("/datetime", "\"2017-01-02T03:04:05.000Z\"")).isEqualTo("\"2017-01-02T03:04:05Z\"");
        assertThat(fetch("/datetime", "\"2017-01-02T03:04:05.000000Z\"")).isEqualTo("\"2017-01-02T03:04:05Z\"");
        assertThat(fetch("/datetime", "\"2017-01-02T03:04:05.000000000Z\""))
                .isEqualTo("\"2017-01-02T03:04:05Z\"");
        assertThat(fetch("/datetime", "\"2017-01-02T04:04:05.000000000+01:00\""))
                .isEqualTo("\"2017-01-02T04:04:05+01:00\"");
        assertThat(fetch("/datetime", "\"2017-01-02T05:04:05.000000000+02:00\""))
                .isEqualTo("\"2017-01-02T05:04:05+02:00\"");
        assertThat(fetch("/binary", "\"123abc\"")).isEqualTo("\"123abc\"");
    }

    private String fetch(String path, String data) {
        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), bytes);
        Request jsonStringRequest = new Request.Builder()
                .url(server.clientConfiguration().uris().get(0) + "/serialization" + path)
                .post(requestBody)
                .build();
        try {
            Response response = okhttpClient.newCall(jsonStringRequest).execute();
            assertThat(response.code()).as("Response is successful").isEqualTo(200);
            return response.body().string();
        } catch (IOException e) {
            throw new RuntimeException("Failed to execute call", e);
        }
    }
}
