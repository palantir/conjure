/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
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
import org.junit.Test;

public class FieldDefinitionValidatorTest {

    @Test
    public void testNoComplexKeysInMaps() {
        String illegalFieldName = "asdf";
        Type complexKeyType = Type.list(ListType.of(Type.primitive(PrimitiveType.STRING)));
        FieldDefinition fieldDefinition = FieldDefinition.of(
                FieldName.of(illegalFieldName),
                Type.map(MapType.of(complexKeyType, Type.primitive(PrimitiveType.STRING))),
                Documentation.of("docs"));
        assertThatThrownBy(() -> FieldDefinitionValidator.validate(fieldDefinition))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(illegalFieldName)
                .hasMessageContaining(complexKeyType.toString());
    }
}
