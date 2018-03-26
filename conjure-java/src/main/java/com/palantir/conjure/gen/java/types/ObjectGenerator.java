/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;


import com.google.common.collect.ImmutableSet;
import com.palantir.conjure.defs.types.TypeDefinitionVisitor;
import com.palantir.conjure.gen.java.ExperimentalFeatures;
import com.palantir.conjure.gen.java.Settings;
import com.palantir.conjure.spec.ErrorDefinition;
import com.palantir.conjure.spec.TypeDefinition;
import com.squareup.javapoet.JavaFile;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class ObjectGenerator implements TypeGenerator {

    private final Set<ExperimentalFeatures> enabledExperimentalFeatures;
    private final Settings settings;

    public ObjectGenerator(Settings settings) {
        this(settings, ImmutableSet.of());
    }

    public ObjectGenerator(Settings settings, Set<ExperimentalFeatures> enabledExperimentalFeatures) {
        this.settings = settings;
        this.enabledExperimentalFeatures = ImmutableSet.copyOf(enabledExperimentalFeatures);
    }

    @Override
    public Set<JavaFile> generateTypes(List<TypeDefinition> types) {
        TypeMapper typeMapper = new TypeMapper(types);

        return types.stream().map(typeDef -> {
            if (typeDef.accept(TypeDefinitionVisitor.IS_OBJECT)) {
                return BeanGenerator.generateBeanType(typeMapper,
                        typeDef.accept(TypeDefinitionVisitor.OBJECT), settings.ignoreUnknownProperties(),
                        enabledExperimentalFeatures);
            } else if (typeDef.accept(TypeDefinitionVisitor.IS_UNION)) {
                return UnionGenerator.generateUnionType(
                        typeMapper, typeDef.accept(TypeDefinitionVisitor.UNION),
                        enabledExperimentalFeatures);
            } else if (typeDef.accept(TypeDefinitionVisitor.IS_ENUM)) {
                return EnumGenerator.generateEnumType(
                        typeDef.accept(TypeDefinitionVisitor.ENUM), settings.supportUnknownEnumValues(),
                        enabledExperimentalFeatures);
            } else if (typeDef.accept(TypeDefinitionVisitor.IS_ALIAS)) {
                return AliasGenerator.generateAliasType(
                        typeMapper, typeDef.accept(TypeDefinitionVisitor.ALIAS), enabledExperimentalFeatures);
            } else {
                throw new IllegalArgumentException("Unknown object definition type " + typeDef.getClass());
            }
        }).collect(Collectors.toSet());
    }

    @Override
    public Set<JavaFile> generateErrors(List<TypeDefinition> types, List<ErrorDefinition> errors) {
        if (errors.isEmpty()) {
            return ImmutableSet.of();
        }
        requireExperimentalFeature(ExperimentalFeatures.ErrorTypes);

        TypeMapper typeMapper = new TypeMapper(types);
        return ErrorGenerator.generateErrorTypes(typeMapper, errors);
    }

    private void requireExperimentalFeature(ExperimentalFeatures feature) {
        if (!enabledExperimentalFeatures.contains(feature)) {
            throw new ExperimentalFeatureDisabledException(feature);
        }
    }

}
