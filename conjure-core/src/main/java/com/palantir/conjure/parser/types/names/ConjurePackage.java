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
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import java.util.List;
import java.util.regex.Pattern;
import org.immutables.value.Value;

/**
 * Represents a package of conjure entities. Syntactically, conjure-packages have the canonical Java style of
 * dot-separated [a-z] words. Generators for different languages translate packages into language-specific notions of
 * "package".
 */
@Value.Immutable
@ConjureImmutablesStyle
public abstract class ConjurePackage {

    @JsonValue
    public abstract String name();

    /** Returns the components of the package, i.e. return {@code {abc, efg}} for package {@code abc.efg}. */
    @Value.Lazy
    public List<String> components() {
        return ImmutableList.copyOf(Splitter.on('.').split(name()));
    }

    @Value.Check
    protected final void check() {
        // TODO(rfink): NPE when the pattern is static !?!?
        Pattern validPackage = Pattern.compile("^([a-z][a-z0-9]+(\\.[a-z][a-z0-9]*)*)?$");
        Preconditions.checkArgument(
                validPackage.matcher(name()).matches(),
                "Conjure package names must match pattern %s: %s",
                validPackage.pattern(),
                name());
    }

    @JsonCreator
    public static ConjurePackage of(String name) {
        return ImmutableConjurePackage.builder().name(name).build();
    }

    public static ConjurePackage of(Iterable<String> components) {
        return of(Joiner.on('.').join(components));
    }
}
