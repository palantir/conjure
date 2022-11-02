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
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.parser.types.TypesDefinition;
import java.util.regex.Pattern;
import org.immutables.value.Value;

/**
 * Represents the name of a namespace for conjure imports, cf. {@link TypesDefinition#conjureImports()}.
 */
@Value.Immutable
@ConjureImmutablesStyle
public abstract class Namespace {

    private static final Pattern NAMESPACE_PATTERN = Pattern.compile("^[_a-zA-Z][_a-zA-Z0-9]*$");

    @JsonValue
    public abstract String name();

    @Value.Check
    protected final void check() {
        Preconditions.checkArgument(
                NAMESPACE_PATTERN.matcher(name()).matches(),
                "Namespaces must match pattern %s: %s",
                NAMESPACE_PATTERN,
                name());
    }

    @JsonCreator
    public static Namespace of(String name) {
        return ImmutableNamespace.builder().name(Names.intern(name)).build();
    }
}
