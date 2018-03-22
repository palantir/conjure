/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.palantir.conjure.defs.Conjure;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.gen.java.services.JerseyServiceGenerator;
import com.palantir.conjure.lib.SafeLong;
import com.palantir.product.EmptyPathService;
import com.palantir.product.EteService;
import com.palantir.remoting3.jaxrs.JaxRsClient;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class JerseyServiceEteTest extends TestBase {

    @ClassRule
    public static final TemporaryFolder folder = new TemporaryFolder();

    @Rule
    public final WitchcraftEteServer server = new WitchcraftEteServer();
    private final EteService client;

    public JerseyServiceEteTest() {
        client = JaxRsClient.create(
                EteService.class,
                server.clientUserAgent(),
                server.clientConfiguration());

        server.witchcraft().api(new EteResource());
        server.witchcraft().api(new EmptyPathResource());
    }

    @Test
    public void jaxrs_client_can_make_a_call_to_an_empty_path() throws Exception {
        EmptyPathService emptyPathClient = JaxRsClient.create(
                EmptyPathService.class,
                server.clientUserAgent(),
                server.clientConfiguration());
        assertThat(emptyPathClient.emptyPath()).isEqualTo(true);
    }

    @Ignore // https://github.palantir.build/foundry/conjure/issues/182
    @Test
    public void http_remoting_client_can_retrieve_a_string_from_witchcraft() throws Exception {
        assertThat(client.string(AuthHeader.valueOf("authHeader")))
                .isEqualTo("Hello, world!");
    }

    @Test
    public void http_remoting_client_can_retrieve_a_double_from_witchcraft() throws Exception {
        assertThat(client.double_(AuthHeader.valueOf("authHeader")))
                .isEqualTo(1 / 3d);
    }

    @Test
    public void http_remoting_client_can_retrieve_a_boolean_from_witchcraft() throws Exception {
        assertThat(client.boolean_(AuthHeader.valueOf("authHeader")))
                .isEqualTo(true);
    }

    @Test
    public void http_remoting_client_can_retrieve_a_safelong_from_witchcraft() throws Exception {
        assertThat(client.safelong(AuthHeader.valueOf("authHeader")))
                .isEqualTo(SafeLong.of(12345));
    }

    @Test
    public void http_remoting_client_can_retrieve_an_rid_from_witchcraft() throws Exception {
        assertThat(client.rid(AuthHeader.valueOf("authHeader")))
                .isEqualTo(ResourceIdentifier.of("ri.foundry.main.dataset.1234"));
    }

    @Ignore // https://github.palantir.build/foundry/conjure/issues/182
    @Test
    public void http_remoting_client_can_retrieve_an_optional_string_from_witchcraft() throws Exception {
        assertThat(client.optionalString(AuthHeader.valueOf("authHeader")))
                .isEqualTo(Optional.of("foo"));
    }

    @Test
    public void jaxrs_client_can_retrieve_an_optional_empty_from_witchcraft() throws Exception {
        assertThat(client.optionalEmpty(AuthHeader.valueOf("authHeader")))
                .isEqualTo(Optional.empty());
    }

    @Test
    public void http_remoting_client_can_retrieve_a_date_time_from_witchcraft() throws Exception {
        assertThat(client.datetime(AuthHeader.valueOf("authHeader")))
                .isEqualTo(ZonedDateTime.ofInstant(Instant.ofEpochMilli(1234), ZoneId.from(ZoneOffset.UTC)));
    }

    @BeforeClass
    public static void beforeClass() throws IOException {
        ConjureDefinition def = Conjure.parse(
                ImmutableList.of(new File("src/test/resources/ete-service.yml")));
        List<Path> files = new JerseyServiceGenerator(Collections.emptySet()).emit(def, folder.getRoot());

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
