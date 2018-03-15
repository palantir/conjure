/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.python.types;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.palantir.conjure.defs.types.ConjureTypeVisitor;
import com.palantir.conjure.defs.types.TypeDefinition;
import com.palantir.conjure.defs.types.builtin.AnyType;
import com.palantir.conjure.defs.types.builtin.BinaryType;
import com.palantir.conjure.defs.types.builtin.DateTimeType;
import com.palantir.conjure.defs.types.collect.ListType;
import com.palantir.conjure.defs.types.collect.MapType;
import com.palantir.conjure.defs.types.collect.OptionalType;
import com.palantir.conjure.defs.types.collect.SetType;
import com.palantir.conjure.defs.types.names.ConjurePackage;
import com.palantir.conjure.defs.types.names.TypeName;
import com.palantir.conjure.defs.types.primitive.PrimitiveType;
import com.palantir.conjure.defs.types.reference.ExternalType;
import com.palantir.conjure.defs.types.reference.LocalReferenceType;
import com.palantir.conjure.gen.python.PackageNameProcessor;
import com.palantir.conjure.gen.python.poet.PythonClassName;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ReferencedTypeNameVisitor implements ConjureTypeVisitor<Set<PythonClassName>> {

    private final Map<TypeName, TypeDefinition> typesByName;
    private final PackageNameProcessor packageNameProcessor;

    public ReferencedTypeNameVisitor(List<TypeDefinition> types, PackageNameProcessor packageNameProcessor) {
        this.typesByName = Maps.uniqueIndex(types, t -> t.typeName());
        this.packageNameProcessor = packageNameProcessor;
    }

    @Override
    public Set<PythonClassName> visitAny(AnyType type) {
        return ImmutableSet.of();
    }

    @Override
    public Set<PythonClassName> visitList(ListType type) {
        return type.itemType().visit(this);
    }

    @Override
    public Set<PythonClassName> visitMap(MapType type) {
        return ImmutableSet.<PythonClassName>builder()
                .addAll(type.keyType().visit(this))
                .addAll(type.valueType().visit(this))
                .build();
    }

    @Override
    public Set<PythonClassName> visitOptional(OptionalType type) {
        return type.itemType().visit(this);
    }

    @Override
    public Set<PythonClassName> visitPrimitive(PrimitiveType type) {
        return ImmutableSet.of();
    }

    @Override
    public Set<PythonClassName> visitLocalReference(LocalReferenceType refType) {
        TypeDefinition type = typesByName.get(refType.type());

        if (type != null) {
            ConjurePackage packageName = packageNameProcessor.getPackageName(type.typeName().conjurePackage());
            // TODO(rfink): We do we generate with the package of the reference but the name of the referee?
            return ImmutableSet.of(PythonClassName.of(packageName, refType.type().name()));
        } else {
            return ImmutableSet.of();
        }
    }

    @Override
    public Set<PythonClassName> visitExternal(ExternalType externalType) {
        return visitPrimitive(externalType.fallback());
    }

    @Override
    public Set<PythonClassName> visitSet(SetType type) {
        return type.itemType().visit(this);
    }

    @Override
    public Set<PythonClassName> visitBinary(BinaryType type) {
        return ImmutableSet.of();
    }

    @Override
    public Set<PythonClassName> visitDateTime(DateTimeType type) {
        return ImmutableSet.of();
    }

}
