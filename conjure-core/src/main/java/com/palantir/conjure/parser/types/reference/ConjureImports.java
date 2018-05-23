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
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.parser.ConjureDefinition;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface ConjureImports {

    /**
     * The file from which types are to be imported. The path is relative to the directory in which the declaring
     * top-level ConjureDefinition file lives.
     */
    String file();

    ConjureDefinition conjure();

    @JsonCreator
    static ConjureImports fromFile(String file) {
        return ImmutableConjureImports.builder()
                .file(file)
                // When deserializing this object from user-supplied conjure yaml, we just fill in the file.
                .conjure(ConjureDefinition.builder().build())
                .build();
    }

    static ConjureImports withResolvedImports(String file, ConjureDefinition conjureDefinition) {
        return ImmutableConjureImports.builder()
                .file(file)
                .conjure(conjureDefinition)
                .build();
    }
}
