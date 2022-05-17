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

package com.palantir.conjure.defs;

import com.palantir.conjure.parser.AnnotatedConjureSourceFile;
import com.palantir.conjure.parser.ConjureParser;
import com.palantir.conjure.parser.NormalizeDefinition;
import com.palantir.conjure.spec.ConjureDefinition;
import java.io.File;
import java.util.Collection;
import java.util.Map;

public final class Conjure {
    public static final Integer SUPPORTED_IR_VERSION = 1;

    private Conjure() {}

    /**
     * Deserializes {@link ConjureDefinition} from their YAML representations in the given files.
     *
     * @deprecated in favor of {@link #parse(ConjureArgs)}
     */
    @Deprecated
    public static ConjureDefinition parse(Collection<File> files) {
        return parse(ConjureArgs.builder()
                .definitions(files)
                .safetyDeclarations(SafetyDeclarationRequirements.ALLOWED)
                .build());
    }

    /**
     * Deserializes {@link ConjureDefinition} from their YAML representations in the given files.
     */
    public static ConjureDefinition parse(ConjureArgs args) {
        Map<String, AnnotatedConjureSourceFile> sourceFiles = ConjureParser.parseAnnotated(args.definitions());
        ConjureDefinition ir = ConjureParserUtils.parseConjureDef(sourceFiles, args.safetyDeclarations());
        return NormalizeDefinition.normalize(ir);
    }
}
