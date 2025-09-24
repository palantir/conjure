/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
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

import com.google.common.collect.ImmutableList;
import com.palantir.conjure.exceptions.ConjureRuntimeException;
import com.palantir.conjure.parser.ConjureParser;
import com.palantir.conjure.spec.ConjureDefinition;
import com.palantir.conjure.spec.Documentation;
import com.palantir.conjure.spec.EndpointError;
import com.palantir.conjure.spec.ErrorNamespace;
import com.palantir.conjure.spec.ErrorTypeName;
import com.palantir.logsafe.exceptions.SafeIllegalArgumentException;
import java.io.File;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;

public class ConjureDefTest {

    @Test
    public void resolvesImportedAliases() {
        @SuppressWarnings("for-rollout:deprecation")
        ConjureDefinition conjureDefinition = ConjureParserUtils.parseConjureDef(
                ConjureParser.parseAnnotated(new File("src/test/resources/example-conjure-imports.yml")));
        assertThat(conjureDefinition.getTypes()).hasSize(3);
    }

    @Test
    public void resolvesRecursiveImportType() {
        @SuppressWarnings("for-rollout:deprecation")
        ConjureDefinition conjureDefinition = ConjureParserUtils.parseConjureDef(
                ConjureParser.parseAnnotated(new File("src/test/resources/example-recursive-imports.yml")));
        assertThat(conjureDefinition.getTypes()).hasSize(1);
    }

    @Test
    public void resolvesCircularType_singleFile() {
        @SuppressWarnings("for-rollout:deprecation")
        ConjureDefinition conjureDefinition = ConjureParserUtils.parseConjureDef(
                ConjureParser.parseAnnotated(new File("src/test/resources/example-circular.yml")));
        assertThat(conjureDefinition.getTypes()).hasSize(2);
    }

    @Test
    public void resolvesCircularType_multiFile() {
        @SuppressWarnings("for-rollout:deprecation")
        ConjureDefinition conjureDefinition =
                ConjureParserUtils.parseConjureDef(ConjureParser.parseAnnotated(ImmutableList.of(
                        new File("src/test/resources/example-multi-file-circular-import-a.yml"),
                        new File("src/test/resources/example-multi-file-circular-import-b.yml"))));
        assertThat(conjureDefinition.getTypes()).hasSize(2);
    }

    // Test currently fails as it attempts to parse a TypeScript package name as a java package
    @SuppressWarnings("for-rollout:deprecation")
    @Test
    @Ignore
    public void handlesNonJavaExternalType() {
        ConjureParserUtils.parseConjureDef(
                ConjureParser.parseAnnotated(new File("src/test/resources/example-external-types.yml")));
    }

    @Test
    public void namelessService() {
        @SuppressWarnings("for-rollout:deprecation")
        ConjureDefinition conjureDefinition = ConjureParserUtils.parseConjureDef(
                ConjureParser.parseAnnotated(new File("src/test/resources/nameless-test-service.yml")));
        assertThat(conjureDefinition.getServices()).hasSize(1);
    }

    @SuppressWarnings("for-rollout:deprecation")
    @Test
    public void testThrowsWhenEndpointErrorDefinitionNotAReference() {
        assertThatThrownBy(() -> ConjureParserUtils.parseConjureDef(ConjureParser.parseAnnotated(
                        new File("src/test/resources/example-non-reference-endpoint-error.yml"))))
                .isInstanceOf(ConjureRuntimeException.class)
                .rootCause()
                .isInstanceOf(SafeIllegalArgumentException.class)
                .hasMessage("Unsupported endpoint error type. Endpoint errors must be references to a Conjure-defined "
                        + "error type: {type=INTEGER}");
    }

    @SuppressWarnings("for-rollout:deprecation")
    @Test
    public void testThrowsWhenEndpointErrorIsUndefined() {
        assertThatThrownBy(() -> ConjureParserUtils.parseConjureDef(ConjureParser.parseAnnotated(
                        new File("src/test/resources/example-non-existent-endpoint-error.yml"))))
                .isInstanceOf(ConjureRuntimeException.class)
                .rootCause()
                .isInstanceOf(SafeIllegalArgumentException.class)
                .hasMessage("Unknown error: {error=NonExistentError}");
    }

    @Test
    public void testEndpointErrorsCanBeImported() {
        @SuppressWarnings("for-rollout:deprecation")
        ConjureDefinition conjureDefinition = ConjureParserUtils.parseConjureDef(
                ConjureParser.parseAnnotated(new File("src/test/resources/example-imported-endpoint-error.yml")));

        assertThat(conjureDefinition.getServices())
                .filteredOn(serviceDefinition ->
                        serviceDefinition.getServiceName().getName().equals("TestService"))
                .singleElement()
                .satisfies(serviceDefinition -> {
                    assertThat(serviceDefinition.getEndpoints())
                            .singleElement()
                            .satisfies(endpointDefinition -> assertThat(endpointDefinition.getErrors())
                                    .containsExactlyInAnyOrder(
                                            EndpointError.builder()
                                                    .error(ErrorTypeName.builder()
                                                            .name("Error2")
                                                            .package_("test.api.with.imported.errors")
                                                            .namespace(ErrorNamespace.of("TestNamespace"))
                                                            .build())
                                                    .build(),
                                            // The InvalidArgument is imported from the `test.api` package.
                                            EndpointError.builder()
                                                    .error(ErrorTypeName.builder()
                                                            .name("InvalidArgument")
                                                            .package_("test.api")
                                                            .namespace(ErrorNamespace.of("Test"))
                                                            .build())
                                                    .docs(Documentation.of("Docs for the imported error"))
                                                    .build(),
                                            EndpointError.builder()
                                                    .error(ErrorTypeName.builder()
                                                            .name("InvalidArgument")
                                                            .package_("test.api.with.imported.errors")
                                                            .namespace(ErrorNamespace.of("OtherNamespace"))
                                                            .build())
                                                    .docs(Documentation.of("An error with the same name is imported"
                                                            + " from test-service.yml, but has a different namespace."))
                                                    .build()));
                });
    }

    @SuppressWarnings("for-rollout:deprecation")
    @Test
    void importFailureContainsTransitiveFileName() {
        assertThatThrownBy(() -> ConjureParserUtils.parseConjureDef(
                        ConjureParser.parseAnnotated(new File("src/test/resources/example-transitive-import.yml"))))
                .isInstanceOf(ConjureRuntimeException.class)
                .rootCause()
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("example-missing-import.yml");
    }

    @SuppressWarnings("for-rollout:deprecation")
    @Test
    void nonTransitiveMissingImportDoesNotContainAdditionalFileName() {
        assertThatThrownBy(() -> ConjureParserUtils.parseConjureDef(
                        ConjureParser.parseAnnotated(new File("src/test/resources/example-missing-import.yml"))))
                .isInstanceOf(ConjureRuntimeException.class)
                .rootCause()
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Import not found for namespace: Namespace{name=imports}");
    }
}
