/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;


import com.google.common.collect.ImmutableSet;
import com.palantir.conjure.defs.types.BaseObjectTypeDefinition;
import com.palantir.conjure.defs.types.TypesDefinition;
import com.palantir.conjure.defs.types.complex.EnumTypeDefinition;
import com.palantir.conjure.defs.types.complex.ErrorTypeDefinition;
import com.palantir.conjure.defs.types.complex.ObjectTypeDefinition;
import com.palantir.conjure.defs.types.complex.UnionTypeDefinition;
import com.palantir.conjure.defs.types.reference.AliasTypeDefinition;
import com.palantir.conjure.gen.java.ExperimentalFeatures;
import com.palantir.conjure.gen.java.Settings;
import com.squareup.javapoet.JavaFile;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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
    public JavaFile generateObjectType(TypesDefinition types, BaseObjectTypeDefinition typeDef) {
        TypeMapper typeMapper = new TypeMapper(types);
        if (typeDef instanceof ObjectTypeDefinition) {
            return BeanGenerator.generateBeanType(typeMapper,
                    (ObjectTypeDefinition) typeDef, settings.ignoreUnknownProperties(),
                    enabledExperimentalFeatures);
        } else if (typeDef instanceof UnionTypeDefinition) {
            return UnionGenerator.generateUnionType(
                    typeMapper, (UnionTypeDefinition) typeDef,
                    enabledExperimentalFeatures);
        } else if (typeDef instanceof EnumTypeDefinition) {
            return EnumGenerator.generateEnumType(
                    (EnumTypeDefinition) typeDef, settings.supportUnknownEnumValues(),
                    enabledExperimentalFeatures);
        } else if (typeDef instanceof AliasTypeDefinition) {
            return AliasGenerator.generateAliasType(
                    typeMapper, (AliasTypeDefinition) typeDef, enabledExperimentalFeatures);
        }
        throw new IllegalArgumentException("Unknown object definition type " + typeDef.getClass());
    }

    @Override
    public Set<JavaFile> generateErrorTypes(TypesDefinition allTypes, List<ErrorTypeDefinition> errorTypeNameToDef) {
        if (errorTypeNameToDef.isEmpty()) {
            return Collections.emptySet();
        }

        requireExperimentalFeature(ExperimentalFeatures.ErrorTypes);

        TypeMapper typeMapper = new TypeMapper(allTypes);
        return ErrorGenerator.generateErrorTypes(typeMapper, errorTypeNameToDef);
    }

    private void requireExperimentalFeature(ExperimentalFeatures feature) {
        if (!enabledExperimentalFeatures.contains(feature)) {
            throw new ExperimentalFeatureDisabledException(feature);
        }
    }

}
