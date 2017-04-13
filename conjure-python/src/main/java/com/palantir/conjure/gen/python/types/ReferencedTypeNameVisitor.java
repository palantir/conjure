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
import com.palantir.conjure.defs.types.reference.ConjureImports;
import com.palantir.conjure.defs.types.reference.ExternalTypeDefinition;
import com.palantir.conjure.defs.types.reference.ReferenceType;
import com.palantir.conjure.gen.python.PackageNameProcessor;
import com.palantir.conjure.gen.python.poet.PythonClassName;
import java.util.Optional;
import java.util.Set;

public final class ReferencedTypeNameVisitor implements ConjureTypeVisitor<Set<PythonClassName>> {

    private final TypesDefinition types;
    private final ConjureImports importedTypes;
    private final PackageNameProcessor packageNameProcessor;

    public ReferencedTypeNameVisitor(TypesDefinition types, ConjureImports importedTypes,
            PackageNameProcessor packageNameProcessor) {
        this.types = types;
        this.importedTypes = importedTypes;
        this.packageNameProcessor = packageNameProcessor;
    }

    @Override
    public Set<PythonClassName> visit(AnyType anyType) {
        return ImmutableSet.of();
    }

    @Override
    public Set<PythonClassName> visit(ListType listType) {
        return listType.itemType().visit(this);
    }

    @Override
    public Set<PythonClassName> visit(MapType mapType) {
        return ImmutableSet.<PythonClassName>builder()
                .addAll(mapType.keyType().visit(this))
                .addAll(mapType.valueType().visit(this))
                .build();
    }

    @Override
    public Set<PythonClassName> visit(OptionalType optionalType) {
        return optionalType.itemType().visit(this);
    }

    @Override
    public Set<PythonClassName> visit(PrimitiveType primitiveType) {
        return ImmutableSet.of();
    }

    @Override
    public Set<PythonClassName> visit(ReferenceType refType) {
        if (!refType.namespace().isPresent()) {
            // Types without namespace are either defined locally in this conjure definition, or raw imports.
            BaseObjectTypeDefinition type = types.definitions().objects().get(refType.type());
            if (type != null) {
                ConjurePackage packageName = packageNameProcessor.getPackageName(type.conjurePackage());
                return ImmutableSet.of(PythonClassName.of(packageName, refType.type().name()));
            } else {
                ExternalTypeDefinition depType = types.imports().get(refType.type());
                checkNotNull(depType, "Unable to resolve type %s", refType.type());
                return visit(depType.baseType());
            }
        } else {
            // Types with namespace are imported Conjure types.
            return ImmutableSet.of(PythonClassName.of(
                    packageNameProcessor.getPackageName(Optional.of(importedTypes.getPackage(refType))),
                    refType.type().name()));
        }
    }

    @Override
    public Set<PythonClassName> visit(SetType setType) {
        return setType.itemType().visit(this);
    }

    @Override
    public Set<PythonClassName> visit(BinaryType binaryType) {
        return ImmutableSet.of();
    }

    @Override
    public Set<PythonClassName> visit(SafeLongType safeLongType) {
        return ImmutableSet.of();
    }

    @Override
    public Set<PythonClassName> visit(DateTimeType dateTimeType) {
        return ImmutableSet.of();
    }

}
