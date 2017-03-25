/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.types;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.palantir.conjure.defs.ConjureImports;
import com.palantir.conjure.defs.TypesDefinition;
import com.palantir.conjure.defs.types.AliasTypeDefinition;
import com.palantir.conjure.defs.types.BaseObjectTypeDefinition;
import com.palantir.conjure.defs.types.ConjureType;
import com.palantir.conjure.defs.types.EnumTypeDefinition;
import com.palantir.conjure.defs.types.ObjectTypeDefinition;
import com.palantir.conjure.defs.types.OptionalType;
import com.palantir.conjure.gen.typescript.poet.AssignStatement;
import com.palantir.conjure.gen.typescript.poet.CastExpression;
import com.palantir.conjure.gen.typescript.poet.ExportStatement;
import com.palantir.conjure.gen.typescript.poet.ImportStatement;
import com.palantir.conjure.gen.typescript.poet.JsonExpression;
import com.palantir.conjure.gen.typescript.poet.RawExpression;
import com.palantir.conjure.gen.typescript.poet.StringExpression;
import com.palantir.conjure.gen.typescript.poet.TypescriptExpression;
import com.palantir.conjure.gen.typescript.poet.TypescriptFile;
import com.palantir.conjure.gen.typescript.poet.TypescriptInterface;
import com.palantir.conjure.gen.typescript.poet.TypescriptTypeSignature;
import com.palantir.conjure.gen.typescript.utils.GenerationUtils;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public final class DefaultTypeGenerator implements TypeGenerator {

    @Override
    public Set<TypescriptFile> generate(TypesDefinition types, ConjureImports imports) {
        return types.definitions().objects().entrySet().stream().map(
                type -> generateType(
                        types,
                        imports,
                        types.definitions().defaultPackage(),
                        type.getKey(),
                        type.getValue()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<ExportStatement> generateExports(TypesDefinition types) {
        return types.definitions().objects().entrySet().stream().map(
                type -> generateExport(
                        types,
                        types.definitions().defaultPackage(),
                        type.getKey(),
                        type.getValue()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

    private Optional<TypescriptFile> generateType(TypesDefinition types, ConjureImports imports, String defaultPackage,
            String typeName, BaseObjectTypeDefinition baseTypeDef) {
        String packageLocation = baseTypeDef.packageName().orElse(defaultPackage);
        String parentFolderPath = GenerationUtils.packageNameToFolderPath(packageLocation);
        TypeMapper mapper = new TypeMapper(types, imports, defaultPackage);
        if (baseTypeDef instanceof EnumTypeDefinition) {
            return Optional.of(generateEnumFile(
                    typeName, (EnumTypeDefinition) baseTypeDef, packageLocation, parentFolderPath, mapper));
        } else if (baseTypeDef instanceof ObjectTypeDefinition) {
            return Optional.of(generateObjectFile(
                    typeName, (ObjectTypeDefinition) baseTypeDef, packageLocation, parentFolderPath, mapper));
        } else if (baseTypeDef instanceof AliasTypeDefinition) {
            // in typescript we do nothing with this
            return Optional.absent();
        }
        throw new IllegalArgumentException("Unknown object definition type: " + baseTypeDef.getClass());
    }

    private Optional<ExportStatement> generateExport(TypesDefinition types, String defaultPackage, String typeName,
            BaseObjectTypeDefinition baseTypeDef) {
        String packageLocation = baseTypeDef.packageName().orElse(defaultPackage);
        String parentFolderPath = GenerationUtils.packageNameToFolderPath(packageLocation);
        if (baseTypeDef instanceof EnumTypeDefinition) {
            return Optional.of(
                    GenerationUtils.createExportStatementRelativeToRoot(typeName, parentFolderPath, typeName));
        } else if (baseTypeDef instanceof ObjectTypeDefinition) {
            return Optional.of(
                    GenerationUtils.createExportStatementRelativeToRoot("I" + typeName, parentFolderPath, typeName));
        } else if (baseTypeDef instanceof AliasTypeDefinition) {
            // in typescript we do nothing with this
            return Optional.absent();
        }
        throw new IllegalArgumentException("Unknown object definition type: " + baseTypeDef.getClass());

    }

    private static TypescriptFile generateObjectFile(String typeName, ObjectTypeDefinition typeDef,
            String packageLocation, String parentFolderPath, TypeMapper mapper) {
        Set<TypescriptTypeSignature> propertySignatures = typeDef.fields().entrySet()
                .stream()
                .map(e -> TypescriptTypeSignature.builder()
                        .isOptional(e.getValue().type() instanceof OptionalType)
                        .name(e.getKey())
                        .typescriptType(mapper.getTypescriptType(e.getValue().type()))
                        .build())
                .collect(Collectors.toSet());
        TypescriptInterface thisInterface = TypescriptInterface.builder()
                .name("I" + typeName)
                .propertySignatures(new TreeSet<>(propertySignatures))
                .build();

        List<ConjureType> referencedTypes = typeDef.fields().values().stream()
                .map(e -> e.type()).collect(Collectors.toList());
        List<ImportStatement> importStatements = GenerationUtils.generateImportStatements(referencedTypes,
                typeName, packageLocation, mapper);

        return TypescriptFile.builder().name(typeName).imports(importStatements)
                .addEmittables(thisInterface).parentFolderPath(parentFolderPath).build();
    }

    private static TypescriptFile generateEnumFile(String typeName, EnumTypeDefinition typeDef,
            String packageLocation, String parentFolderPath, TypeMapper mapper) {
        RawExpression typeRhs = RawExpression.of(Joiner.on(" | ").join(
                typeDef.values().stream().map(value -> StringExpression.of(value.value()).emitToString()).collect(
                        Collectors.toList())));
        AssignStatement type = AssignStatement.builder().lhs("export type " + typeName).rhs(typeRhs).build();
        Map<String, TypescriptExpression> jsonMap = typeDef.values().stream().collect(Collectors.toMap(
                value -> value.value(),
                value -> CastExpression.builder()
                        .expression(StringExpression.of(value.value()))
                        .type(StringExpression.of(value.value()).emitToString())
                        .build()));
        JsonExpression constantRhs = JsonExpression.builder().putAllKeyValues(jsonMap).build();
        AssignStatement constant = AssignStatement.builder().lhs("export const " + typeName).rhs(constantRhs).build();
        return TypescriptFile.builder().name(typeName).addEmittables(type).addEmittables(constant).parentFolderPath(
                parentFolderPath).build();
    }
}
