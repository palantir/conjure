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

import com.google.common.base.Preconditions;
import com.palantir.conjure.spec.ErrorDefinition;
import com.palantir.conjure.spec.ExternalReference;
import com.palantir.conjure.spec.ListType;
import com.palantir.conjure.spec.MapType;
import com.palantir.conjure.spec.OptionalType;
import com.palantir.conjure.spec.PrimitiveType;
import com.palantir.conjure.spec.SetType;
import com.palantir.conjure.spec.Type;
import com.palantir.conjure.spec.TypeName;
import com.palantir.logsafe.exceptions.SafeIllegalArgumentException;
import java.util.Map;

public final class TypeToErrorVisitor implements Type.Visitor<ErrorDefinition> {

    private final Map<TypeName, ErrorDefinition> definitions;

    public TypeToErrorVisitor(Map<TypeName, ErrorDefinition> definitions) {
        this.definitions = definitions;
    }

    @Override
    public ErrorDefinition visitPrimitive(PrimitiveType _value) {
        throw new SafeIllegalArgumentException("Expected error type - found primitive!");
    }

    @Override
    public ErrorDefinition visitOptional(OptionalType _value) {
        throw new SafeIllegalArgumentException("Expected error type - found optional!");
    }

    @Override
    public ErrorDefinition visitList(ListType _value) {
        throw new SafeIllegalArgumentException("Expected error type - found list!");
    }

    @Override
    public ErrorDefinition visitSet(SetType _value) {
        throw new SafeIllegalArgumentException("Expected error type - found set!");
    }

    @Override
    public ErrorDefinition visitMap(MapType _value) {
        throw new SafeIllegalArgumentException("Expected error type - found map!");
    }

    @Override
    public ErrorDefinition visitReference(TypeName value) {
        ErrorDefinition definition = definitions.get(value);
        Preconditions.checkNotNull(definition, "No error definition: %s", value.getName());
        return definition;
    }

    @Override
    public ErrorDefinition visitExternal(ExternalReference value) {
        ErrorDefinition definition = definitions.get(value.getExternalReference());
        Preconditions.checkNotNull(
                definition,
                "No error definition: %s",
                value.getExternalReference().getName());
        return definition;
    }

    @Override
    public ErrorDefinition visitUnknown(String _unknownType) {
        throw new SafeIllegalArgumentException("Expected error type - found unknown!");
    }
}
