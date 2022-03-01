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

import com.google.common.collect.ImmutableList;
import com.palantir.conjure.parser.ConjureParser;
import com.palantir.conjure.spec.ConjureDefinition;
import java.io.File;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;

public class ConjureDefTest {

    @Test
    public void resolvesImportedAliases() {
        ConjureDefinition conjureDefinition = ConjureParserUtils.parseConjureDef(
                ConjureParser.parseAnnotated(new File("src/test/resources/example-conjure-imports.yml")));
        assertThat(conjureDefinition.getTypes()).hasSize(3);
    }

    @Test
    public void resolvesRecursiveImportType() {
        ConjureDefinition conjureDefinition = ConjureParserUtils.parseConjureDef(
                ConjureParser.parseAnnotated(new File("src/test/resources/example-recursive-imports.yml")));
        assertThat(conjureDefinition.getTypes()).hasSize(1);
    }

    @Test
    public void resolvesCircularType_singleFile() {
        ConjureDefinition conjureDefinition = ConjureParserUtils.parseConjureDef(
                ConjureParser.parseAnnotated(new File("src/test/resources/example-circular.yml")));
        assertThat(conjureDefinition.getTypes()).hasSize(2);
    }

    @Test
    public void resolvesCircularType_multiFile() {
        ConjureDefinition conjureDefinition =
                ConjureParserUtils.parseConjureDef(ConjureParser.parseAnnotated(ImmutableList.of(
                        new File("src/test/resources/example-multi-file-circular-import-a.yml"),
                        new File("src/test/resources/example-multi-file-circular-import-b.yml"))));
        assertThat(conjureDefinition.getTypes().size()).isEqualTo(2);
    }

    // Test currently fails as it attempts to parse a TypeScript package name as a java package
    @Test
    @Ignore
    public void handlesNonJavaExternalType() {
        ConjureParserUtils.parseConjureDef(
                ConjureParser.parseAnnotated(new File("src/test/resources/example-external-types.yml")));
    }
}
