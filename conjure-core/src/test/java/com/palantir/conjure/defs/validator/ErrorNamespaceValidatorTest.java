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

package com.palantir.conjure.defs.validator;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.palantir.conjure.spec.ErrorNamespace;
import org.junit.Test;

public final class ErrorNamespaceValidatorTest {

    @Test
    public void testValidErrorNamespaces() throws Exception {
        ErrorNamespaceValidator.validate(ErrorNamespace.of("Conjure"));
        ErrorNamespaceValidator.validate(ErrorNamespace.of("PalantirFoo"));
        ErrorNamespaceValidator.validate(ErrorNamespace.of("PalantirFooBar"));
    }

    @Test
    public void testInvalidErrorNamespaces() {
        for (String invalid : new String[] {
                "conjure",
                "palantirFoo",
                "palantir-foo",
                "PALANTIR-FOO",
                "palantir_foo",
                "PALANTIR_FOO"
        }) {
            assertThatThrownBy(() -> ErrorNamespaceValidator.validate(ErrorNamespace.of(invalid)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Namespace for errors must match pattern")
                    .hasMessageContaining(invalid);
        }
    }

}
