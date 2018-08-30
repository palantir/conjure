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

import com.google.common.base.Preconditions;
import com.palantir.conjure.either.Either;
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

/**
 * A type visitor that resolves through any aliases and references and stops once it reaches a non-reference {@link
 * Type}, or a non-alias {@link TypeDefinition}. The intention of this is to prevent cycles, where a named object refers
 * back to itself down the line.
 * <p>
 * If it encounters a {@link TypeDefinition} that is NOT an {@link AliasDefinition}, then it
 * returns {@link Either#left}, otherwise it returns the {@link Either#right} of the de-aliased {@link Type} that will
 * not be a {@link Type#reference}.
 */
public final class DealiasingTypeVisitor implements Type.Visitor<Either<TypeDefinition, Type>> {
    private final Map<TypeName, TypeDefinition> objects;

    public DealiasingTypeVisitor(Map<TypeName, TypeDefinition> objects) {
        this.objects = objects;
    }

    public Either<TypeDefinition, Type> dealias(Type type) {
        return type.accept(this);
    }

    @Override
    public Either<TypeDefinition, Type> visitReference(TypeName value) {
        TypeDefinition typeDefinition = objects.get(value);
        Preconditions.checkState(
                typeDefinition != null,
                "Referenced TypeDefinition not found in map of types for TypeName: %s", value);
        return typeDefinition.accept(new TypeDefinition.Visitor<Either<TypeDefinition, Type>>() {
            @Override
            public Either<TypeDefinition, Type> visitAlias(AliasDefinition value) {
                // Recursively visit target of alias
                return value.getAlias().accept(DealiasingTypeVisitor.this);
            }

            @Override
            public Either<TypeDefinition, Type> visitEnum(EnumDefinition value) {
                return Either.left(TypeDefinition.enum_(value));
            }

            @Override
            public Either<TypeDefinition, Type> visitObject(ObjectDefinition value) {
                return Either.left(TypeDefinition.object(value));
            }

            @Override
            public Either<TypeDefinition, Type> visitUnion(UnionDefinition value) {
                return Either.left(TypeDefinition.union(value));
            }

            @Override
            public Either<TypeDefinition, Type> visitUnknown(String unknownType) {
                throw new IllegalStateException("Unsupported type: " + unknownType);
            }
        });
    }

    // Identity mapping for here onwards.

    @Override
    public Either<TypeDefinition, Type> visitPrimitive(PrimitiveType value) {
        return Either.right(Type.primitive(value));
    }

    @Override
    public Either<TypeDefinition, Type> visitOptional(OptionalType value) {
        return Either.right(Type.optional(value));
    }

    @Override
    public Either<TypeDefinition, Type> visitList(ListType value) {
        return Either.right(Type.list(value));
    }

    @Override
    public Either<TypeDefinition, Type> visitSet(SetType value) {
        return Either.right(Type.set(value));
    }

    @Override
    public Either<TypeDefinition, Type> visitMap(MapType value) {
        return Either.right(Type.map(value));
    }

    @Override
    public Either<TypeDefinition, Type> visitExternal(ExternalReference value) {
        return Either.right(Type.external(value));
    }

    @Override
    public Either<TypeDefinition, Type> visitUnknown(String unknownType) {
        throw new IllegalStateException("Unsupported type: " + unknownType);
    }
}
