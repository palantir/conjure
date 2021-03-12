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

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.parser.ConjureSourceFile;
import com.palantir.conjure.parser.types.names.TypeName;
import org.immutables.value.Value;

/**
 * Represents a reference to a builtin type or a type defined local to the containing {@link ConjureSourceFile} (as
 * opposed to a {@link ForeignReferenceType} which references a type defined in an {@link
 * com.palantir.conjure.parser.types.TypesDefinition#conjureImports imported type}).
 */
@Value.Immutable
@ConjureImmutablesStyle
public interface LocalReferenceError extends ReferenceError {

    static LocalReferenceError of(TypeName type) {
        return ImmutableLocalReferenceError.builder().type(type).build();
    }
}
