/*
 * (c) Copyright 2025 Palantir Technologies Inc. All rights reserved.
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

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.palantir.conjure.defs.ConjureTypeParserVisitor.ByParsedRepresentationTypeNameResolver;
import com.palantir.conjure.defs.ConjureTypeParserVisitor.ReferenceTypeResolver;
import com.palantir.conjure.parser.types.TypesDefinition;
import com.palantir.conjure.parser.types.names.Namespace;
import com.palantir.conjure.parser.types.names.TypeName;
import com.palantir.conjure.parser.types.reference.ForeignReferenceType;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class ConjureTypeParserVisitorTest {

    @Test
    void errorMessageIncludesTransitiveFileNameWhenResolvingForeignType() {
        String conjureSourceFileName = "/test/conjure.yml";
        ReferenceTypeResolver resolver = new ByParsedRepresentationTypeNameResolver(
                TypesDefinition.builder().build(), Map.of(), Map.of(), conjureSourceFileName);
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(
                        () -> resolver.resolve(ForeignReferenceType.of(Namespace.of("namespace"), TypeName.of("Type"))))
                .withMessageContaining(conjureSourceFileName);
    }
}
