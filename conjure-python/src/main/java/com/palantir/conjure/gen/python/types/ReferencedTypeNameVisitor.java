/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.python.types;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableSet;
import com.palantir.conjure.defs.types.BaseObjectTypeDefinition;
import com.palantir.conjure.defs.types.ConjureTypeVisitor;
import com.palantir.conjure.defs.types.TypesDefinition;
import com.palantir.conjure.defs.types.builtin.AnyType;
import com.palantir.conjure.defs.types.builtin.BinaryType;
import com.palantir.conjure.defs.types.builtin.DateTimeType;
import com.palantir.conjure.defs.types.builtin.SafeLongType;
import com.palantir.conjure.defs.types.collect.ListType;
import com.palantir.conjure.defs.types.collect.MapType;
import com.palantir.conjure.defs.types.collect.OptionalType;
import com.palantir.conjure.defs.types.collect.SetType;
import com.palantir.conjure.defs.types.names.ConjurePackage;
import com.palantir.conjure.defs.types.primitive.PrimitiveType;
import com.palantir.conjure.defs.types.reference.ExternalTypeDefinition;
import com.palantir.conjure.defs.types.reference.ForeignReferenceType;
import com.palantir.conjure.defs.types.reference.LocalReferenceType;
import com.palantir.conjure.gen.python.PackageNameProcessor;
import com.palantir.conjure.gen.python.poet.PythonClassName;
import java.util.Optional;
import java.util.Set;

public final class ReferencedTypeNameVisitor implements ConjureTypeVisitor<Set<PythonClassName>> {

    private final TypesDefinition types;
    private final PackageNameProcessor packageNameProcessor;

    public ReferencedTypeNameVisitor(TypesDefinition types,
            PackageNameProcessor packageNameProcessor) {
        this.types = types;
        this.packageNameProcessor = packageNameProcessor;
    }

    @Override
    public Set<PythonClassName> visit(AnyType type) {
        return ImmutableSet.of();
    }

    @Override
    public Set<PythonClassName> visit(ListType type) {
        return type.itemType().visit(this);
    }

    @Override
    public Set<PythonClassName> visit(MapType type) {
        return ImmutableSet.<PythonClassName>builder()
                .addAll(type.keyType().visit(this))
                .addAll(type.valueType().visit(this))
                .build();
    }

    @Override
    public Set<PythonClassName> visit(OptionalType type) {
        return type.itemType().visit(this);
    }

    @Override
    public Set<PythonClassName> visit(PrimitiveType type) {
        return ImmutableSet.of();
    }

    @Override
    public Set<PythonClassName> visit(LocalReferenceType refType) {
        BaseObjectTypeDefinition type = types.definitions().objects().get(refType.type());
        if (type != null) {
            ConjurePackage packageName = packageNameProcessor.getPackageName(type.conjurePackage());
            return ImmutableSet.of(PythonClassName.of(packageName, refType.type().name()));
        } else {
            ExternalTypeDefinition depType = types.imports().get(refType.type());
            checkNotNull(depType, "Unable to resolve type %s", refType.type());
            return visit(depType.baseType());
        }
    }

    @Override
    public Set<PythonClassName> visit(ForeignReferenceType type) {
        ConjurePackage importPackage = types.getImportsForRefNameSpace(type).getPackageForImportedType(type);
        return ImmutableSet.of(PythonClassName.of(
                packageNameProcessor.getPackageName(Optional.of(importPackage)),
                type.type().name()));
    }

    @Override
    public Set<PythonClassName> visit(SetType type) {
        return type.itemType().visit(this);
    }

    @Override
    public Set<PythonClassName> visit(BinaryType type) {
        return ImmutableSet.of();
    }

    @Override
    public Set<PythonClassName> visit(SafeLongType type) {
        return ImmutableSet.of();
    }

    @Override
    public Set<PythonClassName> visit(DateTimeType type) {
        return ImmutableSet.of();
    }

}
