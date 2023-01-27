/*
 * (c) Copyright 2023 Palantir Technologies Inc. All rights reserved.
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

import com.palantir.conjure.spec.ExternalReference;
import com.palantir.conjure.spec.ListType;
import com.palantir.conjure.spec.MapType;
import com.palantir.conjure.spec.OptionalType;
import com.palantir.conjure.spec.PrimitiveType;
import com.palantir.conjure.spec.SetType;
import com.palantir.conjure.spec.Type;
import com.palantir.conjure.spec.TypeName;
import java.util.Optional;

enum ExtractExternalReferenceVisitor implements Type.Visitor<Optional<ExternalReference>> {
    INSTANCE;

    @Override
    public Optional<ExternalReference> visitPrimitive(PrimitiveType _value) {
        return Optional.empty();
    }

    @Override
    public Optional<ExternalReference> visitOptional(OptionalType _value) {
        return Optional.empty();
    }

    @Override
    public Optional<ExternalReference> visitList(ListType _value) {
        return Optional.empty();
    }

    @Override
    public Optional<ExternalReference> visitSet(SetType _value) {
        return Optional.empty();
    }

    @Override
    public Optional<ExternalReference> visitMap(MapType _value) {
        return Optional.empty();
    }

    @Override
    public Optional<ExternalReference> visitReference(TypeName _value) {
        return Optional.empty();
    }

    @Override
    public Optional<ExternalReference> visitExternal(ExternalReference value) {
        return Optional.of(value);
    }

    @Override
    public Optional<ExternalReference> visitUnknown(String _unknownType) {
        return Optional.empty();
    }
}
