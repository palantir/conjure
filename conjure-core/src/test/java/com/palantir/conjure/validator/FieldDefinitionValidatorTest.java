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

package com.palantir.conjure.validator;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.palantir.conjure.defs.validator.FieldDefinitionValidator;
import com.palantir.conjure.spec.Documentation;
import com.palantir.conjure.spec.FieldDefinition;
import com.palantir.conjure.spec.FieldName;
import com.palantir.conjure.spec.ListType;
import com.palantir.conjure.spec.MapType;
import com.palantir.conjure.spec.PrimitiveType;
import com.palantir.conjure.spec.Type;
import org.junit.jupiter.api.Test;

public class FieldDefinitionValidatorTest {
    private static final Type STRING_TYPE = Type.primitive(PrimitiveType.STRING);
    private static final Type COMPLEX_KEY_TYPE = Type.list(ListType.of(Type.primitive(PrimitiveType.STRING)));

    @Test
    public void testNoComplexKeysInMaps() {
        assertInvalidType(Type.map(MapType.of(COMPLEX_KEY_TYPE, STRING_TYPE)));
    }

    @Test
    public void testNoComplexKeysInMapsInLists() {
        assertInvalidType(Type.list(ListType.of(Type.map(MapType.of(COMPLEX_KEY_TYPE, STRING_TYPE)))));
    }

    @Test
    public void testNoComplexKeysInDeepMaps() {
        assertInvalidType(Type.map(MapType.of(STRING_TYPE, Type.map(MapType.of(COMPLEX_KEY_TYPE, STRING_TYPE)))));
    }

    private void assertInvalidType(Type type) {
        String illegalFieldName = "asdf";
        FieldDefinition fieldDefinition = FieldDefinition.builder()
                .fieldName(FieldName.of(illegalFieldName))
                .type(type)
                .docs(Documentation.of("docs"))
                .build();
        assertThatThrownBy(() -> FieldDefinitionValidator.validate(fieldDefinition))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(illegalFieldName)
                .hasMessageContaining(COMPLEX_KEY_TYPE.toString());
    }
}
