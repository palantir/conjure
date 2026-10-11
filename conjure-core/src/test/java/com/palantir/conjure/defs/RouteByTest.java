/*
 * (c) Copyright 2026 Palantir Technologies Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.palantir.conjure.defs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.palantir.conjure.parser.ConjureParser;
import com.palantir.conjure.spec.EndpointDefinition;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

final class RouteByTest {
    @TempDir
    Path directory;

    @Test
    void preservesSelectorOrderAndResolvesAliasesAndOptionalFields() throws IOException {
        assertThat(parse("[options.context.enrollmentRid, tableRid]").getRouteBy())
                .containsExactly("options.context.enrollmentRid", "tableRid");
        assertThat(parse("[]").getRouteBy()).isEmpty();
    }

    @Test
    void supportsBinaryAndBearerTokenFieldsIncludingAliasesAndOptionals() throws IOException {
        assertThat(parse("[options.binary, options.binaryAlias, options.token, options.tokenAlias]")
                        .getRouteBy())
                .containsExactly("options.binary", "options.binaryAlias", "options.token", "options.tokenAlias");
        assertThat(parse("[options]", "bearertoken").getRouteBy()).containsExactly("options");
        assertThat(parse("[options]", "TokenAlias").getRouteBy()).containsExactly("options");
    }

    @ParameterizedTest
    @ValueSource(strings = {"binary", "BinaryAlias"})
    void rejectsSelectingAnEntireBinaryBody(String type) {
        assertThatThrownBy(() -> parse("[options]", type))
                .isInstanceOf(RuntimeException.class)
                .hasStackTraceContaining("route-by binary selectors must select a body field");
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "[missing]",
                "[options.missing]",
                "[options]",
                "[options.context]",
                "[options.items]",
                "[options.any]",
                "[tableRid, tableRid]",
                "[tableRid.field]",
                "[options..context]",
                "[header]",
                "[options.external]",
                "[options.externalAlias]",
                "true",
                "tableRid"
            })
    void rejectsInvalidSelectors(String selectors) {
        assertThatThrownBy(() -> parse(selectors)).isInstanceOf(RuntimeException.class);
    }

    private EndpointDefinition parse(String selectors) throws IOException {
        return parse(selectors, "Options");
    }

    private EndpointDefinition parse(String selectors, String bodyType) throws IOException {
        Path definition = directory.resolve("routing.yml");
        Files.writeString(definition, """
            types:
              imports:
                External:
                  base-type: string
                  external:
                    java: test.api.External
              definitions:
                default-package: test.api
                objects:
                  RidAlias:
                    alias: rid
                  BinaryAlias:
                    alias: binary
                  TokenAlias:
                    alias: bearertoken
                  ExternalAlias:
                    alias: optional<External>
                  Context:
                    fields:
                      enrollmentRid: optional<RidAlias>
                  Options:
                    fields:
                      context: optional<Context>
                      items: list<string>
                      token: bearertoken
                      tokenAlias: optional<TokenAlias>
                      binary: binary
                      binaryAlias: optional<BinaryAlias>
                      any: any
                      external: External
                      externalAlias: ExternalAlias
            services:
              RoutingService:
                name: Routing
                package: test.api
                default-auth: none
                base-path: /
                endpoints:
                  route:
                    http: POST /tables/{tableRid}
                    route-by: %s
                    args:
                      tableRid:
                        type: RidAlias
                        param-type: path
                      options:
                        type: %s
                        param-type: body
                      header:
                        type: string
                        param-type: header
                        param-id: X-Route
                    returns: string
            """.formatted(selectors, bodyType));
        return ConjureParserUtils.parseConjureDef(ConjureParser.parseAnnotated(definition.toFile()))
                .getServices()
                .get(0)
                .getEndpoints()
                .get(0);
    }
}
