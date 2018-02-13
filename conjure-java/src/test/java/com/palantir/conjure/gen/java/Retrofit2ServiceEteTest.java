/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import com.palantir.conjure.defs.Conjure;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.gen.java.services.Retrofit2ServiceGenerator;
import com.palantir.conjure.lib.SafeLong;
import com.palantir.product.EteServiceRetrofit;
import com.palantir.remoting3.retrofit2.Retrofit2Client;
import com.palantir.ri.ResourceIdentifier;
import com.palantir.tokens.auth.AuthHeader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class Retrofit2ServiceEteTest extends TestBase {

    @ClassRule
    public static final TemporaryFolder folder = new TemporaryFolder();

    @Rule
    public final WitchcraftEteServer server = new WitchcraftEteServer();
    private final EteServiceRetrofit client;

    public Retrofit2ServiceEteTest() {
        this.client = Retrofit2Client.create(
                EteServiceRetrofit.class,
                server.clientUserAgent(),
                server.clientConfiguration()
        );
        server.witchcraft().api(new EteResource());
    }

    @Ignore // https://github.palantir.build/foundry/conjure/issues/182
    @Test
    public void retrofit2_can_retrieve_a_string_from_witchcraft() throws Exception {
        assertThat(client.string(AuthHeader.valueOf("authHeader")).execute().body())
                .isEqualTo("Hello, world!");
    }

    @Test
    public void retrofit2_client_can_retrieve_a_double_from_witchcraft() throws Exception {
        assertThat(client.double_(AuthHeader.valueOf("authHeader")).execute().body())
                .isEqualTo(1 / 3d);
    }

    @Test
    public void retrofit2_client_can_retrieve_a_boolean_from_witchcraft() throws Exception {
        assertThat(client.boolean_(AuthHeader.valueOf("authHeader")).execute().body())
                .isEqualTo(true);
    }

    @Test
    public void retrofit2_client_can_retrieve_a_safelong_from_witchcraft() throws Exception {
        assertThat(client.safelong(AuthHeader.valueOf("authHeader")).execute().body())
                .isEqualTo(SafeLong.of(12345));
    }

    @Test
    public void retrofit2_client_can_retrieve_an_rid_from_witchcraft() throws Exception {
        assertThat(client.rid(AuthHeader.valueOf("authHeader")).execute().body())
                .isEqualTo(ResourceIdentifier.of("ri.foundry.main.dataset.1234"));
    }

    @Ignore // https://github.palantir.build/foundry/conjure/issues/182
    @Test
    public void retrofit2_client_can_retrieve_an_optional_string_from_witchcraft() throws Exception {
        assertThat(client.optionalString(AuthHeader.valueOf("authHeader")).execute().body())
                .isEqualTo(Optional.of("foo"));
    }

    @Ignore // https://github.com/palantir/http-remoting/issues/668
    @Test
    public void retrofit2_client_can_retrieve_an_optional_empty_from_witchcraft() throws Exception {
        assertThat(client.optionalEmpty(AuthHeader.valueOf("authHeader")).execute().body())
                .isEqualTo(Optional.empty());
    }

    @Test
    public void retrofit2_client_can_retrieve_a_date_time_from_witchcraft() throws Exception {
        assertThat(client.datetime(AuthHeader.valueOf("authHeader")).execute().body())
                .isEqualTo(ZonedDateTime.ofInstant(Instant.ofEpochMilli(1234), ZoneId.from(ZoneOffset.UTC)));
    }

    @Test
    public void retrofit2_client_can_retrieve_binary_data_from_witchcraft() throws Exception {
        assertThat(client.binary(AuthHeader.valueOf("authHeader")).execute().body().string())
                .isEqualTo("Hello, world!");
    }

    @BeforeClass
    public static void beforeClass() throws IOException {
        ConjureDefinition def = Conjure.parse(new File("src/test/resources/ete-service.yml"));
        List<Path> files = new Retrofit2ServiceGenerator(
                ImmutableSet.of(ExperimentalFeatures.DisambiguateRetrofitServices)).emit(def, folder.getRoot());

        for (Path file : files) {
            Path output = Paths.get("src/integrationInput/java/com/palantir/product/" + file.getFileName());
            if (Boolean.valueOf(System.getProperty("recreate", "false"))) {
                Files.deleteIfExists(output);
                Files.copy(file, output);
            }

            assertThat(readFromFile(file)).isEqualTo(readFromFile(output));
        }
    }
}
