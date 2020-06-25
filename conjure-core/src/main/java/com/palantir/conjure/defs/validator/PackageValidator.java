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

package com.palantir.conjure.defs.validator;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.regex.Pattern;

public final class PackageValidator {

    /** Primitive types have an empty package. */
    public static final String PRIMITIVE = "";

    private PackageValidator() {}

    private static final Pattern VALID_PACKAGE = Pattern.compile("^([a-z][a-z0-9]+(\\.[a-z][a-z0-9]*)*)?$");

    public static List<String> components(String name) {
        return ImmutableList.copyOf(Splitter.on('.').split(name));
    }

    public static void validate(String name) {
        // TODO(rfink): NPE when the pattern is static !?!?

        Preconditions.checkArgument(
                VALID_PACKAGE.matcher(name).matches(),
                "Conjure package names must match pattern %s: %s",
                VALID_PACKAGE.pattern(),
                name);
    }

    public static String conjurePackage(Iterable<String> components) {
        String path = Joiner.on('.').join(components);
        validate(path);
        return path;
    }
}
