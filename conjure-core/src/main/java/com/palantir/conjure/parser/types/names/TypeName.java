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

package com.palantir.conjure.parser.types.names;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.parser.types.NamedTypesDefinition;
import java.util.regex.Pattern;
import org.immutables.value.Value;

/**
 * Represents the name of a conjure {@link NamedTypesDefinition#objects() object}.
 */
@com.google.errorprone.annotations.Immutable
@Value.Immutable
@ConjureImmutablesStyle
public abstract class TypeName {

    private static final Pattern CUSTOM_TYPE_PATTERN = Pattern.compile("^[A-Z][a-z0-9]+([A-Z][a-z0-9]+)*$");
    static final ImmutableSet<String> PRIMITIVE_TYPES =
            ImmutableSet.of("any", "string", "integer", "double", "boolean", "safelong", "rid", "bearertoken", "uuid");

    @JsonValue
    public abstract String name();

    @Value.Check
    protected final void check() {
        Preconditions.checkArgument(
                CUSTOM_TYPE_PATTERN.matcher(name()).matches() || PRIMITIVE_TYPES.contains(name()),
                "TypeNames must be a primitive type %s or match pattern %s: %s",
                PRIMITIVE_TYPES,
                CUSTOM_TYPE_PATTERN,
                name());
    }

    @JsonCreator
    public static TypeName of(String name) {
        return ImmutableTypeName.builder().name(name).build();
    }
}
