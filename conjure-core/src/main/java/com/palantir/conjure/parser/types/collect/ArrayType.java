/*
 * (c) Copyright 2024 Palantir Technologies Inc. All rights reserved.
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

package com.palantir.conjure.parser.types.collect;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.parser.types.ConjureType;
import com.palantir.conjure.parser.types.ConjureTypeVisitor;
import com.palantir.conjure.parser.types.primitive.PrimitiveType;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface ArrayType extends ConjureType {

    @JsonProperty("item-type")
    PrimitiveType itemType();

    @Override
    default <T> T visit(ConjureTypeVisitor<T> visitor) {
        return visitor.visitArray(this);
    }

    static ArrayType of(PrimitiveType itemType) {
        return ImmutableArrayType.builder().itemType(itemType).build();
    }
}
