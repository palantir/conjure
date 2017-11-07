/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.complex;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.Test;

public final class EnumValueDefinitionTest {

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
    @SuppressWarnings("CheckReturnValue")
    public void testFormat() {
        EnumValueDefinition.builder().value("FOO").build();
        EnumValueDefinition.builder().value("FOO_BAR").build();
        EnumValueDefinition.builder().value("FOO_123_BAR").build();
        EnumValueDefinition.builder().value("F12").build();

        for (String value : new String[] {
                "foo",
                "fooBar",
                "FOO-BAR",
                " - a",
                " - a_b",
                " - A__B",
                " - _A",
                " - A_",
                "123_FOO"
        }) {
            assertThatThrownBy(() -> EnumValueDefinition.builder().value(value).build())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Enumeration values must match format [A-Z][A-Z0-9]*(_[A-Z0-9]+)*: %s", value);
        }
    }
}
