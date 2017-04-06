/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.types;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.palantir.conjure.defs.ConjureImports;
import com.palantir.conjure.defs.ObjectDefinitions;
import com.palantir.conjure.defs.TypesDefinition;
import com.palantir.conjure.defs.types.AliasTypeDefinition;
import com.palantir.conjure.defs.types.AnyType;
import com.palantir.conjure.defs.types.BaseObjectTypeDefinition;
import com.palantir.conjure.defs.types.ConjureType;
import com.palantir.conjure.defs.types.EnumTypeDefinition;
import com.palantir.conjure.defs.types.ObjectTypeDefinition;
import com.palantir.conjure.defs.types.OptionalType;
import com.palantir.conjure.defs.types.PrimitiveType;
import com.palantir.conjure.defs.types.UnionTypeDefinition;
import com.palantir.conjure.defs.types.UnionValueDefinition;
import com.palantir.conjure.gen.typescript.poet.AssignStatement;
import com.palantir.conjure.gen.typescript.poet.CastExpression;
import com.palantir.conjure.gen.typescript.poet.EqualityStatement;
import com.palantir.conjure.gen.typescript.poet.ExportStatement;
import com.palantir.conjure.gen.typescript.poet.ImportStatement;
import com.palantir.conjure.gen.typescript.poet.JsonExpression;
import com.palantir.conjure.gen.typescript.poet.RawExpression;
import com.palantir.conjure.gen.typescript.poet.ReturnStatement;
import com.palantir.conjure.gen.typescript.poet.StringExpression;
import com.palantir.conjure.gen.typescript.poet.TypescriptEqualityClause;
import com.palantir.conjure.gen.typescript.poet.TypescriptExpression;
import com.palantir.conjure.gen.typescript.poet.TypescriptFile;
import com.palantir.conjure.gen.typescript.poet.TypescriptFunction;
import com.palantir.conjure.gen.typescript.poet.TypescriptFunctionBody;
import com.palantir.conjure.gen.typescript.poet.TypescriptFunctionSignature;
import com.palantir.conjure.gen.typescript.poet.TypescriptInterface;
import com.palantir.conjure.gen.typescript.poet.TypescriptType;
import com.palantir.conjure.gen.typescript.poet.TypescriptTypeSignature;
import com.palantir.conjure.gen.typescript.utils.GenerationUtils;
import com.palantir.parsec.ParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

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

    private Optional<TypescriptFile> generateType(TypesDefinition types, ConjureImports imports,
            Optional<String> defaultPackage, String typeName, BaseObjectTypeDefinition baseTypeDef) {
        String packageLocation = ObjectDefinitions.getPackageName(baseTypeDef.packageName(), defaultPackage, typeName);
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
            return Optional.empty();
        } else if (baseTypeDef instanceof UnionTypeDefinition) {
            return Optional.of(generateUnionTypeFile(
                    typeName, (UnionTypeDefinition) baseTypeDef, packageLocation, parentFolderPath, mapper));
        }
        throw new IllegalArgumentException("Unknown object definition type: " + baseTypeDef.getClass());
    }

    private Optional<ExportStatement> generateExport(TypesDefinition types, Optional<String> defaultPackage,
            String typeName, BaseObjectTypeDefinition baseTypeDef) {
        String packageLocation = ObjectDefinitions.getPackageName(baseTypeDef.packageName(), defaultPackage, typeName);
        String parentFolderPath = GenerationUtils.packageNameToFolderPath(packageLocation);
        if (baseTypeDef instanceof EnumTypeDefinition) {
            return Optional.of(
                    GenerationUtils.createExportStatementRelativeToRoot(typeName, parentFolderPath, typeName));
        } else if (baseTypeDef instanceof ObjectTypeDefinition || baseTypeDef instanceof UnionTypeDefinition) {
            return Optional.of(
                    GenerationUtils.createExportStatementRelativeToRoot("I" + typeName, parentFolderPath, typeName));
        } else if (baseTypeDef instanceof AliasTypeDefinition) {
            // in typescript we do nothing with this
            return Optional.empty();
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

    private TypescriptFile generateUnionTypeFile(String typeName, UnionTypeDefinition baseTypeDef,
            String packageLocation, String parentFolderPath, TypeMapper mapper) {
        Set<UnionValueDefinition> unions = baseTypeDef.union();
        List<ConjureType> referencedTypes = Lists.newArrayList();
        SortedSet<TypescriptTypeSignature> propertySignatures = new TreeSet<TypescriptTypeSignature>();
        propertySignatures.add(TypescriptTypeSignature.builder()
                .name("type")
                .typescriptType(mapper.getTypescriptType(PrimitiveType.STRING))
                .build());
        SortedSet<TypescriptFunction> methods =
                new TreeSet<TypescriptFunction>(Comparator.comparing(TypescriptFunction::functionSignature));
        ReturnStatement baseReturn = ReturnStatement.builder().expression(RawExpression.of("undefined")).build();
        String interfaceName = "I" + typeName;
        TypescriptType unionType = TypescriptType.builder().name(interfaceName).build();
        for (UnionValueDefinition union : unions) {
            String unionValue = union.value();
            String unionValueUncapitalized = StringUtils.uncapitalize(unionValue);
            ConjureType conjureTypeOfUnionValue = getConjureType(unionValue);
            referencedTypes.add(conjureTypeOfUnionValue);
            propertySignatures.add(
                    TypescriptTypeSignature.builder()
                            .name(StringExpression.of(unionValueUncapitalized).emitToString())
                            .typescriptType(mapper.getTypescriptType(conjureTypeOfUnionValue))
                            .isOptional(true)
                            .build());
            TypescriptFunctionSignature functionHeader =
                    TypescriptFunctionSignature.builder()
                            .addParameters(
                                    TypescriptTypeSignature.builder().name("obj").typescriptType(unionType).build())
                            .name(simpleName(unionValueUncapitalized))
                            .build();
            TypescriptEqualityClause typescriptEqualityClause = TypescriptEqualityClause.builder()
                    .clause("obj.type === " + StringExpression.of(unionValue).emitToString())
                    .build();
            EqualityStatement equalityStatement = EqualityStatement.builder()
                    .typescriptEqualityStatement(typescriptEqualityClause)
                    .equalityBody(ReturnStatement.builder()
                            .expression(
                                    RawExpression.of(
                                            "obj[" + StringExpression.of(unionValueUncapitalized).emitToString() + "]"))
                            .build())
                    .build();
            TypescriptFunction helperFunction = TypescriptFunction.builder()
                    .functionSignature(functionHeader)
                    .functionBody(TypescriptFunctionBody.builder()
                            .addStatements(equalityStatement)
                            .addStatements(baseReturn).build())
                    .isStatic(true)
                    .build();
            methods.add(helperFunction);
        }
        propertySignatures.add(TypescriptTypeSignature.builder()
                .name("[key: string]")
                .typescriptType(mapper.getTypescriptType(AnyType.of()))
                .build());
        TypescriptInterface thisInterface = TypescriptInterface.builder()
                .name(interfaceName)
                .propertySignatures(propertySignatures)
                .build();
        List<ImportStatement> importStatements = GenerationUtils.generateImportStatements(referencedTypes,
                typeName, packageLocation, mapper);
        return TypescriptFile.builder()
                .name(typeName)
                .imports(importStatements)
                .addEmittables(thisInterface)
                .addAllEmittables(methods)
                .parentFolderPath(parentFolderPath)
                .build();
    }

    private ConjureType getConjureType(String type) {
        try {
            return ConjureType.fromString(type);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    // Example set<string> -> setString
    private static String simpleName(String rawName) {
        int startingIndex = rawName.indexOf("<");
        int endingIndex = rawName.indexOf(">");
        if (startingIndex > -1 && endingIndex > -1) {
            String dataStructureName = rawName.substring(0, startingIndex);
            // Parameterized type
            int multiParamaterIndex = rawName.indexOf(",");
            if (multiParamaterIndex > -1) {
                String firstParam = StringUtils.capitalize(rawName.substring(startingIndex + 1, multiParamaterIndex));
                String secondParam =
                        StringUtils
                                .capitalize(StringUtils.strip(rawName.substring(multiParamaterIndex + 1, endingIndex)));
                return dataStructureName + firstParam + secondParam;
            } else {
                return dataStructureName + StringUtils.capitalize(rawName.substring(startingIndex + 1, endingIndex));
            }
        }
        return rawName;
    }
}
