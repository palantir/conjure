/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
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

    public static final PrimitiveTypeVisitor PRIMITIVE = new PrimitiveTypeVisitor();
    public static final MapTypeVisitor MAP = new MapTypeVisitor();
    public static final ListTypeVisitor LIST = new ListTypeVisitor();
    public static final SetTypeVisitor SET = new SetTypeVisitor();
    public static final OptionalTypeVisitor OPTIONAL = new OptionalTypeVisitor();
    public static final ReferenceTypeVisitor REFERENCE = new ReferenceTypeVisitor();

    public static final IsPrimitiveTypeVisitor IS_PRIMITIVE = new IsPrimitiveTypeVisitor();
    public static final IsOptionalTypeVisitor IS_OPTIONAL = new IsOptionalTypeVisitor();
    public static final IsListTypeVisitor IS_LIST = new IsListTypeVisitor();
    public static final IsSetTypeVisitor IS_SET = new IsSetTypeVisitor();
    public static final IsMapTypeVisitor IS_MAP = new IsMapTypeVisitor();
    public static final IsReferenceTypeVisitor IS_REFERENCE = new IsReferenceTypeVisitor();

    public static final IsPrimitiveOrReferenceType IS_PRIMITIVE_OR_REFERENCE = new IsPrimitiveOrReferenceType();
    public static final IsBinaryType IS_BINARY = new IsBinaryType();
    public static final IsAnyType IS_ANY = new IsAnyType();

    private static class IsPrimitiveTypeVisitor extends IsTypeVisitor {
        @Override
        public Boolean visitPrimitive(PrimitiveType value) {
            return true;
        }
    }

    private static class IsOptionalTypeVisitor extends IsTypeVisitor {
        @Override
        public Boolean visitOptional(OptionalType value) {
            return true;
        }
    }

    private static class IsListTypeVisitor extends IsTypeVisitor {
        @Override
        public Boolean visitList(ListType value) {
            return true;
        }
    }

    private static class IsSetTypeVisitor extends IsTypeVisitor {
        @Override
        public Boolean visitSet(SetType value) {
            return true;
        }
    }

    private static class IsMapTypeVisitor extends IsTypeVisitor {
        @Override
        public Boolean visitMap(MapType value) {
            return true;
        }
    }

    private static class IsReferenceTypeVisitor extends IsTypeVisitor {
        @Override
        public Boolean visitReference(TypeName value) {
            return true;
        }

        @Override
        public Boolean visitExternal(ExternalReference value) {
            return true;
        }
    }

    private static class IsPrimitiveOrReferenceType extends IsTypeVisitor {
        @Override
        public Boolean visitPrimitive(PrimitiveType value) {
            return true;
        }

        @Override
        public Boolean visitReference(TypeName value) {
            return true;
        }

        @Override
        public Boolean visitExternal(ExternalReference value) {
            return true;
        }
    }

    private static class IsBinaryType extends IsTypeVisitor {
        @Override
        public Boolean visitPrimitive(PrimitiveType value) {
            return value.get() == PrimitiveType.Value.BINARY;
        }
    }

    private static class IsAnyType extends IsTypeVisitor {
        @Override
        public Boolean visitPrimitive(PrimitiveType value) {
            return value.get() == PrimitiveType.Value.ANY;
        }
    }

    private static class IsTypeVisitor implements Type.Visitor<Boolean> {
        @Override
        public Boolean visitPrimitive(PrimitiveType value) {
            return false;
        }

        @Override
        public Boolean visitOptional(OptionalType value) {
            return false;
        }

        @Override
        public Boolean visitList(ListType value) {
            return false;
        }

        @Override
        public Boolean visitSet(SetType value) {
            return false;
        }

        @Override
        public Boolean visitMap(MapType value) {
            return false;
        }

        @Override
        public Boolean visitReference(TypeName value) {
            return false;
        }

        @Override
        public Boolean visitExternal(ExternalReference value) {
            return false;
        }

        @Override
        public Boolean visitUnknown(String unknownType) {
            return false;
        }
    }

    private static class DefaultTypeVisitor<T> implements Type.Visitor<T> {

        @Override
        public T visitPrimitive(PrimitiveType value) {
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

    private static class PrimitiveTypeVisitor extends DefaultTypeVisitor<PrimitiveType> {
        @Override
        public PrimitiveType visitPrimitive(PrimitiveType value) {
            return value;
        }
    }

    private static class ReferenceTypeVisitor extends DefaultTypeVisitor<TypeName> {
        @Override
        public TypeName visitReference(TypeName value) {
            return value;
        }

        @Override
        public TypeName visitExternal(ExternalReference value) {
            return value.getExternalReference();
        }
    }

    private static class MapTypeVisitor extends DefaultTypeVisitor<MapType> {
        @Override
        public MapType visitMap(MapType value) {
            return value;
        }
    }

    private static class ListTypeVisitor extends DefaultTypeVisitor<ListType> {
        @Override
        public ListType visitList(ListType value) {
            return value;
        }
    }

    private static class SetTypeVisitor extends DefaultTypeVisitor<SetType> {
        @Override
        public SetType visitSet(SetType value) {
            return value;
        }
    }

    private static class OptionalTypeVisitor extends DefaultTypeVisitor<OptionalType> {
        @Override
        public OptionalType visitOptional(OptionalType value) {
            return value;
        }
    }
}
