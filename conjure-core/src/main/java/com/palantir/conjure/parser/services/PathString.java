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

package com.palantir.conjure.parser.services;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.logsafe.Unsafe;
import com.palantir.util.syntacticpath.Path;
import com.palantir.util.syntacticpath.Paths;
import org.immutables.value.Value;

/** Represents a HTTP path in a {@link ServiceDefinition conjure service definition}. */
@Unsafe
@Value.Immutable
@ConjureImmutablesStyle
public abstract class PathString {

    /** Returns the well-formed path associated with this path definition. */
    public abstract Path path();

    /**
     * Returns this path "concatenated" with the given other path. For example, {@code "/abc".resolve("/def")} is the
     * path {@code /abc/def}.
     */
    public PathString resolve(PathString other) {
        final Path newPath;
        if (other.path().equals(Path.ROOT_PATH)) {
            // special-case since Path#relativize() only works on proper prefixes
            newPath = path();
        } else {
            newPath = path().resolve(Path.ROOT_PATH.relativize(other.path()));
        }
        return ImmutablePathString.builder().path(newPath).build();
    }

    /** Creates a new {@link PathString} from the given string, or throws an exception if it fails to validate. */
    @JsonCreator
    public static PathString of(String path) {
        Path parsed = Paths.get(path);
        return ImmutablePathString.builder().path(parsed).build();
    }

    @Unsafe
    @Override
    public final String toString() {
        return path().toString();
    }
}
