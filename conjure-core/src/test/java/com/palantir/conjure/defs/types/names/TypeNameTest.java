/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.names;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.Sets;
import com.palantir.conjure.defs.types.primitive.PrimitiveType;
import java.util.Set;
import org.junit.Test;

public final class TypeNameTest {

    private static final ConjurePackage PACKAGE = ConjurePackage.of("package");

    @Test
    public void testValidNames() {
        TypeName.of("Camel", PACKAGE);
        TypeName.of("CamelCase", PACKAGE);
        TypeName.of("CamelCase1", PACKAGE);
        TypeName.of("Camel1Case2", PACKAGE);

        // Primitive types are guaranteed to work due to the static construction in PrimitiveType enum. Anyway.
        Set<String> primitiveTypeNames = Sets.newHashSet();
        primitiveTypeNames.add("unknown");
        for (PrimitiveType primitive : PrimitiveType.values()) {
            primitiveTypeNames.add(primitive.type().name());
        }
        assertThat(TypeName.PRIMITIVE_TYPES).isEqualTo(primitiveTypeNames);
    }

    @Test
    public void testInvalidNames() throws Exception {
        for (String invalid : new String[] {"a", "IFoo", "ABC", "$Special", "snake_case", "kebab-case", "Camel1B"}) {
            assertThatThrownBy(() -> TypeName.of(invalid, PACKAGE))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("TypeNames must be a primitive type"
                            + " [unknown, string, integer, double, boolean, safelong, rid, bearertoken, uuid] or "
                            + "match pattern ^[A-Z][a-z0-9]+([A-Z][a-z0-9]+)*$: %s", invalid);
        }
    }
}
