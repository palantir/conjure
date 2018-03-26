/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.python.types;

import com.google.common.collect.ImmutableSet;
import com.palantir.conjure.defs.types.TypeDefinitionVisitor;
import com.palantir.conjure.defs.types.TypeVisitor;
import com.palantir.conjure.gen.python.PackageNameProcessor;
import com.palantir.conjure.gen.python.poet.PythonClassName;
import com.palantir.conjure.spec.ExternalReference;
import com.palantir.conjure.spec.ListType;
import com.palantir.conjure.spec.MapType;
import com.palantir.conjure.spec.OptionalType;
import com.palantir.conjure.spec.PrimitiveType;
import com.palantir.conjure.spec.SetType;
import com.palantir.conjure.spec.Type;
import com.palantir.conjure.spec.TypeDefinition;
import com.palantir.conjure.spec.TypeName;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class ReferencedTypeNameVisitor implements Type.Visitor<Set<PythonClassName>> {

    private final Set<TypeName> typesByName;
    private final PackageNameProcessor packageNameProcessor;

    public ReferencedTypeNameVisitor(List<TypeDefinition> types, PackageNameProcessor packageNameProcessor) {
        this.typesByName = types.stream()
                .map(type -> type.accept(TypeDefinitionVisitor.TYPE_NAME)).collect(Collectors.toSet());
        this.packageNameProcessor = packageNameProcessor;
    }

    @Override
    public Set<PythonClassName> visitList(ListType type) {
        return type.getItemType().accept(this);
    }

    @Override
    public Set<PythonClassName> visitMap(MapType type) {
        return ImmutableSet.<PythonClassName>builder()
                .addAll(type.getKeyType().accept(this))
                .addAll(type.getValueType().accept(this))
                .build();
    }

    @Override
    public Set<PythonClassName> visitOptional(OptionalType type) {
        return type.getItemType().accept(this);
    }

    @Override
    public Set<PythonClassName> visitPrimitive(PrimitiveType type) {
        return ImmutableSet.of();
    }

    @Override
    public Set<PythonClassName> visitReference(TypeName type) {
        if (typesByName.contains(type)) {
            String packageName = packageNameProcessor.getPackageName(type.getPackage());
            // TODO(rfink): We do we generate with the package of the reference but the name of the referee?
            return ImmutableSet.of(PythonClassName.of(packageName, type.getName()));
        } else {
            return ImmutableSet.of();
        }
    }

    @Override
    public Set<PythonClassName> visitExternal(ExternalReference externalReference) {
        if (externalReference.getFallback().accept(TypeVisitor.IS_PRIMITIVE)) {
            return visitPrimitive(externalReference.getFallback().accept(TypeVisitor.PRIMITIVE));
        } else {
            return ImmutableSet.of();
        }
    }

    @Override
    public Set<PythonClassName> visitUnknown(String unknownType) {
        return ImmutableSet.of();
    }

    @Override
    public Set<PythonClassName> visitSet(SetType type) {
        return type.getItemType().accept(this);
    }
}
