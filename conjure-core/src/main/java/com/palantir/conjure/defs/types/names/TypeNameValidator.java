/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.names;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.palantir.conjure.spec.PrimitiveType;
import com.palantir.conjure.spec.TypeName;
import java.util.List;
import java.util.regex.Pattern;

public final class TypeNameValidator {

    private TypeNameValidator() {}

    private static final Pattern CUSTOM_TYPE_PATTERN = Pattern.compile("^[A-Z][a-z0-9]+([A-Z][a-z0-9]+)*$");

    static final List<String> PRIMITIVE_TYPES = Lists.transform(
            java.util.Arrays.asList(PrimitiveType.Value.values()), value -> value.name());

    public static void validate(TypeName typeName) {
        Preconditions.checkArgument(
                CUSTOM_TYPE_PATTERN.matcher(typeName.getName()).matches()
                        || PRIMITIVE_TYPES.contains(typeName.getName()),
                "TypeNames must be a primitive type %s or match pattern %s: %s",
                PRIMITIVE_TYPES, CUSTOM_TYPE_PATTERN, typeName.getName());
    }
}
