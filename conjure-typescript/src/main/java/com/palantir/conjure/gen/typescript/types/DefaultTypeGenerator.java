/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.types;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.palantir.conjure.defs.types.BaseObjectTypeDefinition;
import com.palantir.conjure.defs.types.ConjureType;
import com.palantir.conjure.defs.types.TypesDefinition;
import com.palantir.conjure.defs.types.collect.OptionalType;
import com.palantir.conjure.defs.types.complex.EnumTypeDefinition;
import com.palantir.conjure.defs.types.complex.ObjectTypeDefinition;
import com.palantir.conjure.defs.types.complex.UnionTypeDefinition;
import com.palantir.conjure.defs.types.names.ConjurePackage;
import com.palantir.conjure.defs.types.names.ConjurePackages;
import com.palantir.conjure.defs.types.names.FieldName;
import com.palantir.conjure.defs.types.names.TypeName;
import com.palantir.conjure.defs.types.reference.AliasTypeDefinition;
import com.palantir.conjure.gen.typescript.poet.AssignStatement;
import com.palantir.conjure.gen.typescript.poet.CastExpression;
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
import com.palantir.conjure.gen.typescript.poet.TypescriptSimpleType;
import com.palantir.conjure.gen.typescript.poet.TypescriptType;
import com.palantir.conjure.gen.typescript.poet.TypescriptTypeAlias;
import com.palantir.conjure.gen.typescript.poet.TypescriptTypeGuardType;
import com.palantir.conjure.gen.typescript.poet.TypescriptTypeSignature;
import com.palantir.conjure.gen.typescript.poet.TypescriptUnionType;
import com.palantir.conjure.gen.typescript.utils.GenerationUtils;
import java.util.Collection;
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
    public Set<TypescriptFile> generate(TypesDefinition types) {
        return types.definitions().objects().entrySet().stream().map(
                type -> generateType(
                        types,
                        types.definitions().defaultConjurePackage(),
                        type.getKey(),
                        type.getValue()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

    @Override
    public Map<ConjurePackage, Collection<ExportStatement>> generateExports(TypesDefinition types) {
        Optional<ConjurePackage> defaultPackage = types.definitions().defaultConjurePackage();
        Map<ConjurePackage, Set<Map.Entry<TypeName, BaseObjectTypeDefinition>>> definitionsbyPackage =
                types.definitions().objects().entrySet().stream()
                        .collect(Collectors.groupingBy(
                                entry -> conjurePackage(entry.getValue(), defaultPackage),
                                Collectors.toSet()));
        return definitionsbyPackage
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(typeAndDefinition -> generateExport(
                                        typeAndDefinition.getKey(), typeAndDefinition.getValue()))
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .collect(Collectors.toSet())));
    }

    private static ConjurePackage conjurePackage(BaseObjectTypeDefinition definition,
            Optional<ConjurePackage> defaultPackage) {
        return definition.conjurePackage().orElse(defaultPackage
                .orElseThrow(() -> new IllegalStateException("object package or default package must be specified")));

    }

    private Optional<TypescriptFile> generateType(TypesDefinition types,
            Optional<ConjurePackage> defaultPackage, TypeName typeName, BaseObjectTypeDefinition baseTypeDef) {
        ConjurePackage packageLocation =
                ConjurePackages.getPackage(baseTypeDef.conjurePackage(), defaultPackage, typeName);
        String parentFolderPath = GenerationUtils.packageToScopeAndModule(packageLocation);
        TypeMapper mapper = new TypeMapper(types, defaultPackage);
        if (baseTypeDef instanceof EnumTypeDefinition) {
            return Optional.of(generateEnumFile(
                    typeName, (EnumTypeDefinition) baseTypeDef, parentFolderPath));
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

    private Optional<ExportStatement> generateExport(TypeName typeName, BaseObjectTypeDefinition baseTypeDef) {
        if (baseTypeDef instanceof EnumTypeDefinition) {
            return Optional.of(GenerationUtils.createExportStatementRelativeToRoot(
                    typeName.name(), typeName.name()));
        } else if (baseTypeDef instanceof ObjectTypeDefinition || baseTypeDef instanceof UnionTypeDefinition) {
            return Optional.of(GenerationUtils.createExportStatementRelativeToRoot(
                    "I" + typeName.name(), typeName.name()));
        } else if (baseTypeDef instanceof AliasTypeDefinition) {
            // in typescript we do nothing with this
            return Optional.empty();
        }
        throw new IllegalArgumentException("Unknown object definition type: " + baseTypeDef.getClass());

    }

    private static TypescriptFile generateObjectFile(TypeName typeName, ObjectTypeDefinition typeDef,
            ConjurePackage packageLocation, String parentFolderPath, TypeMapper mapper) {
        Set<TypescriptTypeSignature> propertySignatures = typeDef.fields().entrySet()
                .stream()
                .map(e -> TypescriptTypeSignature.builder()
                        .isOptional(e.getValue().type() instanceof OptionalType)
                        .name(e.getKey().toCase(FieldName.Case.LOWER_CAMEL_CASE).name())
                        .typescriptType(mapper.getTypescriptType(e.getValue().type()))
                        .build())
                .collect(Collectors.toSet());
        TypescriptInterface thisInterface = TypescriptInterface.builder()
                .name("I" + typeName.name())
                .propertySignatures(new TreeSet<>(propertySignatures))
                .build();

        List<ConjureType> referencedTypes = typeDef.fields().values().stream()
                .map(e -> e.type()).collect(Collectors.toList());
        List<ImportStatement> importStatements = GenerationUtils.generateImportStatements(referencedTypes,
                typeName, packageLocation, mapper);

        return TypescriptFile.builder().name(typeName.name()).imports(importStatements)
                .addEmittables(thisInterface).parentFolderPath(parentFolderPath).build();
    }

    private static TypescriptFile generateEnumFile(
            TypeName typeName, EnumTypeDefinition typeDef, String parentFolderPath) {
        RawExpression typeRhs = RawExpression.of(Joiner.on(" | ").join(
                typeDef.values().stream().map(value -> StringExpression.of(value.value()).emitToString()).collect(
                        Collectors.toList())));
        AssignStatement type = AssignStatement.builder().lhs("export type " + typeName.name()).rhs(typeRhs).build();
        Map<String, TypescriptExpression> jsonMap = typeDef.values().stream().collect(Collectors.toMap(
                value -> value.value(),
                value -> CastExpression.builder()
                        .expression(StringExpression.of(value.value()))
                        .type(StringExpression.of(value.value()).emitToString())
                        .build()));
        JsonExpression constantRhs = JsonExpression.builder().putAllKeyValues(jsonMap).build();
        AssignStatement constant = AssignStatement.builder().lhs(
                "export const " + typeName.name()).rhs(constantRhs).build();
        return TypescriptFile.builder()
                .name(typeName.name())
                .addEmittables(type)
                .addEmittables(constant)
                .parentFolderPath(parentFolderPath)
                .build();
    }

    private TypescriptFile generateUnionTypeFile(TypeName typeName, UnionTypeDefinition baseTypeDef,
            ConjurePackage packageLocation, String parentFolderPath, TypeMapper mapper) {
        List<ConjureType> referencedTypes = Lists.newArrayList();
        List<TypescriptInterface> subInterfaces = Lists.newArrayList();
        List<TypescriptFunction> typeGuards = Lists.newArrayList();
        List<TypescriptFunction> factories = Lists.newArrayList();
        Map<String, TypescriptExpression> helperFunctionProps = Maps.newHashMap();
        String mainName = "I" + typeName.name();
        TypescriptType mainType = TypescriptSimpleType.builder().name(mainName).build();

        baseTypeDef.union().forEach((memberName, memberType) -> {
            String capitalizedMemberName = StringUtils.capitalize(memberName);
            String interfaceName = String.format("%s_%s", mainName, capitalizedMemberName);
            String typeGuardName = String.format("is%s", capitalizedMemberName);
            TypescriptSimpleType interfaceType = TypescriptSimpleType.builder().name(interfaceName).build();
            StringExpression quotedMemberName = StringExpression.of(memberName);
            ConjureType conjureTypeOfMemberType = memberType.type();
            TypescriptSimpleType typescriptMemberType = mapper.getTypescriptType(conjureTypeOfMemberType);
            referencedTypes.add(conjureTypeOfMemberType);


            // build interface
            SortedSet<TypescriptTypeSignature> propertySignatures = Sets.newTreeSet();
            propertySignatures.add(TypescriptTypeSignature.builder()
                    .name("type")
                    .typescriptType(TypescriptSimpleType.builder().name(quotedMemberName.emitToString()).build())
                    .build());
            propertySignatures.add(TypescriptTypeSignature.builder()
                    .name(StringExpression.of(memberName).emitToString())
                    .typescriptType(typescriptMemberType)
                    .build());
            subInterfaces.add(TypescriptInterface.builder()
                    .name(interfaceName)
                    .propertySignatures(propertySignatures)
                    .build());

            // build type guard function
            TypescriptFunctionSignature functionSignature = TypescriptFunctionSignature.builder()
                    .addParameters(TypescriptTypeSignature.builder()
                            .name("obj")
                            .typescriptType(mainType)
                            .build())
                    .name(typeGuardName)
                    .returnType(TypescriptTypeGuardType.builder()
                            .variableName("obj")
                            .predicateType(interfaceType)
                            .build())
                    .build();
            TypescriptFunctionBody functionBody = TypescriptFunctionBody.builder()
                    .addStatements(ReturnStatement.builder()
                            .expression(TypescriptEqualityClause.builder()
                                    .lhs(RawExpression.of("obj.type"))
                                    .rhs(quotedMemberName).build())
                            .build())
                    .build();

            typeGuards.add(TypescriptFunction.builder()
                    .functionSignature(functionSignature)
                    .functionBody(functionBody)
                    .isMethod(false)
                    .build());
            helperFunctionProps.put(typeGuardName, RawExpression.of(typeGuardName));

            // build factory function
            TypescriptFunctionSignature factorySignature = TypescriptFunctionSignature.builder()
                    .addParameters(TypescriptTypeSignature.builder()
                            .name(memberName)
                            .typescriptType(typescriptMemberType)
                            .build())
                    .name(memberName)
                    .returnType(interfaceType)
                    .build();
            TypescriptFunctionBody factoryBody = TypescriptFunctionBody.builder()
                    .addStatements(ReturnStatement.builder()
                            .expression(JsonExpression.builder()
                                    .putKeyValues("type", StringExpression.of(memberName))
                                    .putKeyValues(memberName, RawExpression.of(memberName))
                                    .build())
                            .build())
                    .build();
            factories.add(TypescriptFunction.builder()
                    .functionSignature(factorySignature)
                    .functionBody(factoryBody)
                    .isMethod(false)
                    .build());
            helperFunctionProps.put(memberName, RawExpression.of(memberName));
        });

        List<ImportStatement> importStatements = GenerationUtils.generateImportStatements(referencedTypes,
                typeName, packageLocation, mapper);

        TypescriptUnionType unionType = TypescriptUnionType.builder()
                .types(subInterfaces.stream().map(
                        i -> TypescriptSimpleType.builder().name(i.name()).build()).collect(Collectors.toList()))
                .build();
        TypescriptTypeAlias mainTypeAlias = TypescriptTypeAlias.builder()
                .name(mainName)
                .type(unionType)
                .build();

        AssignStatement typeGuardObj = AssignStatement.builder()
                .lhs("export const " + mainName)
                .rhs(JsonExpression.builder().putAllKeyValues(helperFunctionProps).build())
                .build();

        return TypescriptFile.builder()
                .name(typeName.name())
                .imports(importStatements)
                .addAllEmittables(subInterfaces)
                .addEmittables(mainTypeAlias)
                .addAllEmittables(typeGuards)
                .addAllEmittables(factories)
                .addEmittables(typeGuardObj)
                .parentFolderPath(parentFolderPath)
                .build();
    }

}
