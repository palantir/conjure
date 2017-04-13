/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.validators;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.palantir.conjure.defs.types.EnumValueDefinition;
import org.junit.Test;

public final class EnumValueDefinitionValidatorTest {

    @Test
    public void testUnknownValueNotUsed() {
        for (String value : new String[] {
                "UNKNOWN",
                "Unknown"
        }) {
            assertThatThrownBy(() -> EnumValueDefinition.builder().value(value).build())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("UNKNOWN is a reserved enumeration value and cannot be used in an EnumValueDefinition");
        }
    }

    @Test
    public void testFormat() {
        EnumValueDefinition.builder().value("FOO").build();
        EnumValueDefinition.builder().value("FOO_BAR").build();

        for (String value : new String[] {
                "foo",
                "fooBar",
                "FOO-BAR",
                " - a",
                " - a_b",
                " - A__B",
                " - _A",
                " - A_"
        }) {
            assertThatThrownBy(() -> EnumValueDefinition.builder().value(value).build())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Enumeration values must match format [A-Z]+(_[A-Z]+)*: " + value);
        }
    }
}
