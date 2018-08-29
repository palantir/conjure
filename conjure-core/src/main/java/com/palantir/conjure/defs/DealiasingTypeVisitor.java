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

import com.palantir.conjure.spec.AliasDefinition;
import com.palantir.conjure.spec.EnumDefinition;
import com.palantir.conjure.spec.ExternalReference;
import com.palantir.conjure.spec.ListType;
import com.palantir.conjure.spec.MapType;
import com.palantir.conjure.spec.ObjectDefinition;
import com.palantir.conjure.spec.OptionalType;
import com.palantir.conjure.spec.PrimitiveType;
import com.palantir.conjure.spec.SetType;
import com.palantir.conjure.spec.Type;
import com.palantir.conjure.spec.TypeDefinition;
import com.palantir.conjure.spec.TypeName;
import com.palantir.conjure.spec.UnionDefinition;
import java.util.Map;
import java.util.Optional;

/**
 * A type visitor that resolves through any aliases and references and stops once it reaches a non-reference Type, or
 * a non-alias object.
 * <p>
 * If it encounters a {@link TypeDefinition} that is NOT an {@link AliasDefinition}, then it
 * returns {@link Optional#empty()}, otherwise it returns the de-aliased {@link Type} that will not be a
 * {@link Type#reference}.
 */
public final class DealiasingTypeVisitor implements Type.Visitor<Optional<Type>> {
    private final Map<TypeName, TypeDefinition> objects;

    public DealiasingTypeVisitor(Map<TypeName, TypeDefinition> objects) {
        this.objects = objects;
    }

    @Override
    public Optional<Type> visitReference(TypeName value) {
        return objects.get(value).accept(new TypeDefinition.Visitor<Optional<Type>>() {
            @Override
            public Optional<Type> visitAlias(AliasDefinition value) {
                // Recursively visit target of alias
                return value.getAlias().accept(DealiasingTypeVisitor.this);
            }

            @Override
            public Optional<Type> visitEnum(EnumDefinition value) {
                return Optional.empty();
            }

            @Override
            public Optional<Type> visitObject(ObjectDefinition value) {
                return Optional.empty();
            }

            @Override
            public Optional<Type> visitUnion(UnionDefinition value) {
                return Optional.empty();
            }

            @Override
            public Optional<Type> visitUnknown(String unknownType) {
                throw new IllegalStateException("Unsupported type: " + unknownType);
            }
        });
    }

    // Identity mapping for here onwards.

    @Override
    public Optional<Type> visitPrimitive(PrimitiveType value) {
        return Optional.of(Type.primitive(value));
    }

    @Override
    public Optional<Type> visitOptional(OptionalType value) {
        return Optional.of(Type.optional(value));
    }

    @Override
    public Optional<Type> visitList(ListType value) {
        return Optional.of(Type.list(value));
    }

    @Override
    public Optional<Type> visitSet(SetType value) {
        return Optional.of(Type.set(value));
    }

    @Override
    public Optional<Type> visitMap(MapType value) {
        return Optional.of(Type.map(value));
    }

    @Override
    public Optional<Type> visitExternal(ExternalReference value) {
        return Optional.of(Type.external(value));
    }

    @Override
    public Optional<Type> visitUnknown(String unknownType) {
        throw new IllegalStateException("Unsupported type: " + unknownType);
    }
}
