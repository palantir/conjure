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

package com.palantir.conjure.parser.types.reference;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.logsafe.exceptions.SafeUncheckedIoException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface ConjureImports {

    /**
     * The file from which types are to be imported. The path is relative to the directory in which the declaring
     * top-level ConjureSourceFile file lives.
     */
    String file();

    /**
     * The resolved file from which types are to be imported.
     */
    Optional<File> absoluteFile();

    @JsonCreator
    static ConjureImports relativeFile(String file) {
        return ImmutableConjureImports.builder().file(file).build();
    }

    @Value.Auxiliary
    @JsonIgnore
    default ConjureImports resolve(Path baseDir) {
        try {
            return ImmutableConjureImports.builder()
                    .file(file())
                    .absoluteFile(baseDir.resolve(file()).toFile().getCanonicalFile())
                    .build();
        } catch (IOException e) {
            throw new SafeUncheckedIoException("Couldn't canonicalize file path", e);
        }
    }
}
