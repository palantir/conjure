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
import com.palantir.conjure.parser.types.ConjureTypeVisitor;
import com.palantir.conjure.parser.types.names.Namespace;
import com.palantir.conjure.parser.types.names.TypeName;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface ForeignReferenceType extends ReferenceType {

    Namespace namespace();

    @Override
    default <T> T visit(ConjureTypeVisitor<T> visitor) {
        return visitor.visitForeignReference(this);
    }

    static ForeignReferenceType of(Namespace namespace, TypeName type) {
        return ImmutableForeignReferenceType.builder()
                .namespace(namespace)
                .type(type)
                .build();
    }
}
