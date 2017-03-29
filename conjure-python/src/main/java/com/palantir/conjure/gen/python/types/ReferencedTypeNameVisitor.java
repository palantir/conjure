/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.python.types;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableSet;
import com.palantir.conjure.defs.ConjureImports;
import com.palantir.conjure.defs.TypesDefinition;
import com.palantir.conjure.defs.types.AnyType;
import com.palantir.conjure.defs.types.BaseObjectTypeDefinition;
import com.palantir.conjure.defs.types.BinaryType;
import com.palantir.conjure.defs.types.ConjureTypeVisitor;
import com.palantir.conjure.defs.types.DateTimeType;
import com.palantir.conjure.defs.types.ExternalTypeDefinition;
import com.palantir.conjure.defs.types.ListType;
import com.palantir.conjure.defs.types.MapType;
import com.palantir.conjure.defs.types.OptionalType;
import com.palantir.conjure.defs.types.PrimitiveType;
import com.palantir.conjure.defs.types.ReferenceType;
import com.palantir.conjure.defs.types.SafeLongType;
import com.palantir.conjure.defs.types.SetType;
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
                String packageName = packageNameProcessor.getPackageName(type.packageName());
                return ImmutableSet.of(PythonClassName.of(packageName, refType.type()));
            } else {
                ExternalTypeDefinition depType = types.imports().get(refType.type());
                checkNotNull(depType, "Unable to resolve type %s", refType.type());
                return visit(depType.baseType());
            }
        } else {
            // Types with namespace are imported Conjure types.
            return ImmutableSet.of(PythonClassName.of(
                    packageNameProcessor.getPackageName(Optional.of(importedTypes.getPackage(refType))),
                    refType.type()));
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
