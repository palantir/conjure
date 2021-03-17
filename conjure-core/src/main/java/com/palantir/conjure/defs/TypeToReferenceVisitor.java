/*
 * (c) Copyright 2021 Palantir Technologies Inc. All rights reserved.
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

import com.palantir.conjure.spec.ExternalReference;
import com.palantir.conjure.spec.ListType;
import com.palantir.conjure.spec.MapType;
import com.palantir.conjure.spec.OptionalType;
import com.palantir.conjure.spec.PrimitiveType;
import com.palantir.conjure.spec.SetType;
import com.palantir.conjure.spec.Type;
import com.palantir.conjure.spec.TypeName;
import com.palantir.logsafe.exceptions.SafeIllegalArgumentException;

public final class TypeToReferenceVisitor implements Type.Visitor<TypeName> {

    @Override
    public TypeName visitPrimitive(PrimitiveType _value) {
        throw new SafeIllegalArgumentException("Expected ref type - found primitive!");
    }

    @Override
    public TypeName visitOptional(OptionalType _value) {
        throw new SafeIllegalArgumentException("Expected ref type - found optional!");
    }

    @Override
    public TypeName visitList(ListType _value) {
        throw new SafeIllegalArgumentException("Expected ref type - found list!");
    }

    @Override
    public TypeName visitSet(SetType _value) {
        throw new SafeIllegalArgumentException("Expected ref type - found set!");
    }

    @Override
    public TypeName visitMap(MapType _value) {
        throw new SafeIllegalArgumentException("Expected ref type - found map!");
    }

    @Override
    public TypeName visitReference(TypeName value) {
        return value;
    }

    @Override
    public TypeName visitExternal(ExternalReference value) {
        return value.getExternalReference();
    }

    @Override
    public TypeName visitUnknown(String _unknownType) {
        throw new SafeIllegalArgumentException("Expected error type - found unknown!");
    }
}
