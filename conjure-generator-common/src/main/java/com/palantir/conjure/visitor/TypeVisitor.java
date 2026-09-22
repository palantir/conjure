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

package com.palantir.conjure.visitor;

import com.palantir.conjure.spec.ExternalReference;
import com.palantir.conjure.spec.ListType;
import com.palantir.conjure.spec.MapType;
import com.palantir.conjure.spec.OptionalType;
import com.palantir.conjure.spec.PrimitiveType;
import com.palantir.conjure.spec.SetType;
import com.palantir.conjure.spec.Type;
import com.palantir.conjure.spec.TypeName;

public final class TypeVisitor {

    private TypeVisitor() {}

    @SuppressWarnings("for-rollout:ExposedPrivateType")
    public static final PrimitiveTypeVisitor PRIMITIVE = new PrimitiveTypeVisitor();

    @SuppressWarnings("for-rollout:ExposedPrivateType")
    public static final MapTypeVisitor MAP = new MapTypeVisitor();

    @SuppressWarnings("for-rollout:ExposedPrivateType")
    public static final ListTypeVisitor LIST = new ListTypeVisitor();

    @SuppressWarnings("for-rollout:ExposedPrivateType")
    public static final SetTypeVisitor SET = new SetTypeVisitor();

    @SuppressWarnings("for-rollout:ExposedPrivateType")
    public static final OptionalTypeVisitor OPTIONAL = new OptionalTypeVisitor();

    @SuppressWarnings("for-rollout:ExposedPrivateType")
    public static final ReferenceTypeVisitor REFERENCE = new ReferenceTypeVisitor();

    @SuppressWarnings("for-rollout:ExposedPrivateType")
    public static final IsPrimitiveTypeVisitor IS_PRIMITIVE = new IsPrimitiveTypeVisitor();

    @SuppressWarnings("for-rollout:ExposedPrivateType")
    public static final IsOptionalTypeVisitor IS_OPTIONAL = new IsOptionalTypeVisitor();

    @SuppressWarnings("for-rollout:ExposedPrivateType")
    public static final IsListTypeVisitor IS_LIST = new IsListTypeVisitor();

    @SuppressWarnings("for-rollout:ExposedPrivateType")
    public static final IsSetTypeVisitor IS_SET = new IsSetTypeVisitor();

    @SuppressWarnings("for-rollout:ExposedPrivateType")
    public static final IsMapTypeVisitor IS_MAP = new IsMapTypeVisitor();

    @SuppressWarnings("for-rollout:ExposedPrivateType")
    public static final IsReferenceTypeVisitor IS_REFERENCE = new IsReferenceTypeVisitor();

    @SuppressWarnings("for-rollout:ExposedPrivateType")
    public static final IsPrimitiveOrReferenceType IS_PRIMITIVE_OR_REFERENCE = new IsPrimitiveOrReferenceType();

    @SuppressWarnings("for-rollout:ExposedPrivateType")
    public static final IsBinaryType IS_BINARY = new IsBinaryType();

    @SuppressWarnings("for-rollout:ExposedPrivateType")
    public static final IsAnyType IS_ANY = new IsAnyType();

    private static final class IsPrimitiveTypeVisitor extends IsTypeVisitor {
        @Override
        public Boolean visitPrimitive(PrimitiveType _value) {
            return true;
        }
    }

    private static final class IsOptionalTypeVisitor extends IsTypeVisitor {
        @Override
        public Boolean visitOptional(OptionalType _value) {
            return true;
        }
    }

    private static final class IsListTypeVisitor extends IsTypeVisitor {
        @Override
        public Boolean visitList(ListType _value) {
            return true;
        }
    }

    private static final class IsSetTypeVisitor extends IsTypeVisitor {
        @Override
        public Boolean visitSet(SetType _value) {
            return true;
        }
    }

    private static final class IsMapTypeVisitor extends IsTypeVisitor {
        @Override
        public Boolean visitMap(MapType _value) {
            return true;
        }
    }

    private static final class IsReferenceTypeVisitor extends IsTypeVisitor {
        @Override
        public Boolean visitReference(TypeName _value) {
            return true;
        }

        @Override
        public Boolean visitExternal(ExternalReference _value) {
            return true;
        }
    }

    private static final class IsPrimitiveOrReferenceType extends IsTypeVisitor {
        @Override
        public Boolean visitPrimitive(PrimitiveType _value) {
            return true;
        }

        @Override
        public Boolean visitReference(TypeName _value) {
            return true;
        }

        @Override
        public Boolean visitExternal(ExternalReference _value) {
            return true;
        }
    }

    private static final class IsBinaryType extends IsTypeVisitor {
        @Override
        public Boolean visitPrimitive(PrimitiveType value) {
            return value.get() == PrimitiveType.Value.BINARY;
        }
    }

    private static final class IsAnyType extends IsTypeVisitor {
        @Override
        public Boolean visitPrimitive(PrimitiveType value) {
            return value.get() == PrimitiveType.Value.ANY;
        }
    }

    private static class IsTypeVisitor implements Type.Visitor<Boolean> {
        @Override
        public Boolean visitPrimitive(PrimitiveType _value) {
            return false;
        }

        @Override
        public Boolean visitOptional(OptionalType _value) {
            return false;
        }

        @Override
        public Boolean visitList(ListType _value) {
            return false;
        }

        @Override
        public Boolean visitSet(SetType _value) {
            return false;
        }

        @Override
        public Boolean visitMap(MapType _value) {
            return false;
        }

        @Override
        public Boolean visitReference(TypeName _value) {
            return false;
        }

        @Override
        public Boolean visitExternal(ExternalReference _value) {
            return false;
        }

        @Override
        public Boolean visitUnknown(String _unknownType) {
            return false;
        }
    }

    private static class DefaultTypeVisitor<T> implements Type.Visitor<T> {

        @Override
        public T visitPrimitive(PrimitiveType _value) {
            return null;
        }

        @Override
        public T visitOptional(OptionalType value) {
            throw new IllegalStateException("Unsupported type: " + value);
        }

        @Override
        public T visitList(ListType value) {
            throw new IllegalStateException("Unsupported type: " + value);
        }

        @Override
        public T visitSet(SetType value) {
            throw new IllegalStateException("Unsupported type: " + value);
        }

        @Override
        public T visitMap(MapType value) {
            throw new IllegalStateException("Unsupported type: " + value);
        }

        @Override
        public T visitReference(TypeName value) {
            throw new IllegalStateException("Unsupported type: " + value);
        }

        @Override
        public T visitExternal(ExternalReference value) {
            throw new IllegalStateException("Unsupported type: " + value);
        }

        @Override
        public T visitUnknown(String unknownType) {
            throw new IllegalStateException("Unsupported type: " + unknownType);
        }
    }

    private static final class PrimitiveTypeVisitor extends DefaultTypeVisitor<PrimitiveType> {
        @Override
        public PrimitiveType visitPrimitive(PrimitiveType value) {
            return value;
        }
    }

    private static final class ReferenceTypeVisitor extends DefaultTypeVisitor<TypeName> {
        @Override
        public TypeName visitReference(TypeName value) {
            return value;
        }

        @Override
        public TypeName visitExternal(ExternalReference value) {
            return value.getExternalReference();
        }
    }

    private static final class MapTypeVisitor extends DefaultTypeVisitor<MapType> {
        @Override
        public MapType visitMap(MapType value) {
            return value;
        }
    }

    private static final class ListTypeVisitor extends DefaultTypeVisitor<ListType> {
        @Override
        public ListType visitList(ListType value) {
            return value;
        }
    }

    private static final class SetTypeVisitor extends DefaultTypeVisitor<SetType> {
        @Override
        public SetType visitSet(SetType value) {
            return value;
        }
    }

    private static final class OptionalTypeVisitor extends DefaultTypeVisitor<OptionalType> {
        @Override
        public OptionalType visitOptional(OptionalType value) {
            return value;
        }
    }
}
