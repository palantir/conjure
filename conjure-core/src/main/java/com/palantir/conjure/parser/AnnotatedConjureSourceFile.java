/*
 * (c) Copyright 2020 Palantir Technologies Inc. All rights reserved.
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

package com.palantir.conjure.parser;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.parser.types.names.Namespace;
import com.palantir.logsafe.Unsafe;
import java.io.File;
import java.util.Map;
import org.immutables.value.Value;

@Unsafe
@Value.Immutable
@ConjureImmutablesStyle
public interface AnnotatedConjureSourceFile {
    ConjureSourceFile conjureSourceFile();

    Map<Namespace, String> importProviders();

    File sourceFile();

    class Builder extends ImmutableAnnotatedConjureSourceFile.Builder {}

    static Builder builder() {
        return new Builder();
    }
}
