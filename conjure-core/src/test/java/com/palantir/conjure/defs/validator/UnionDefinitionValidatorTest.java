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

import com.google.common.collect.ImmutableList;
import com.palantir.conjure.defs.ConjureOptions;
import com.palantir.conjure.spec.FieldDefinition;
import com.palantir.conjure.spec.FieldName;
import com.palantir.conjure.spec.PrimitiveType;
import com.palantir.conjure.spec.Type;
import com.palantir.conjure.spec.TypeName;
import com.palantir.conjure.spec.UnionDefinition;
import org.junit.jupiter.api.Test;

public final class UnionDefinitionValidatorTest {

    private static final ConjureOptions OPTIONS =
            ConjureOptions.builder().strict(false).build();

    @Test
    public void testUnionMemberKeyMustNotBeEmpty() {
        FieldDefinition fieldDefinition = FieldDefinition.builder()
                .fieldName(FieldName.of(""))
                .type(Type.primitive(PrimitiveType.STRING))
                .build();

        assertThatThrownBy(() -> UnionDefinitionValidator.validateAll(
                        UnionDefinition.builder()
                                .union(fieldDefinition)
                                .typeName(TypeName.of("string", ""))
                                .build(),
                        OPTIONS))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith("Union member key must not be empty");
    }

    @Test
    public void testUnionMemberKeyMustNotBeIllegalJavaIdentifier() {
        ImmutableList.of("%foo", "foo@").forEach(key -> {
            FieldDefinition fieldDefinition = FieldDefinition.builder()
                    .fieldName(FieldName.of(key))
                    .type(Type.primitive(PrimitiveType.STRING))
                    .build();

            assertThatThrownBy(() -> UnionDefinitionValidator.validateAll(
                            UnionDefinition.builder()
                                    .union(fieldDefinition)
                                    .typeName(TypeName.of("string", ""))
                                    .build(),
                            OPTIONS))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageStartingWith(String.format("Union member key must be a valid Java identifier: %s", key));
        });
    }

    @Test
    public void testUnionMemberKeyMustNotHaveTrailingUnderscore() {
        FieldDefinition fieldDefinition = FieldDefinition.builder()
                .fieldName(FieldName.of("foo_"))
                .type(Type.primitive(PrimitiveType.STRING))
                .build();
        assertThatThrownBy(() -> UnionDefinitionValidator.validateAll(
                        UnionDefinition.builder()
                                .union(fieldDefinition)
                                .typeName(TypeName.of("string", ""))
                                .build(),
                        OPTIONS))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith("Union member key must not end with an underscore: foo_");
    }
}
