/*
 * (c) Copyright 2022 Palantir Technologies Inc. All rights reserved.
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
import java.io.File;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
interface FileWithProvenance {

    File file();

    /**
     * The conjure source file this file was imported from, if known.
     */
    Optional<File> importedFrom();

    static FileWithProvenance of(File file) {
        return ImmutableFileWithProvenance.builder().file(file).build();
    }

    static FileWithProvenance of(File file, File importedFrom) {
        return ImmutableFileWithProvenance.builder()
                .file(file)
                .importedFrom(importedFrom)
                .build();
    }
}
