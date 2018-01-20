/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.utils;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.palantir.conjure.defs.types.ConjureType;
import com.palantir.conjure.defs.types.names.ConjurePackage;
import com.palantir.conjure.defs.types.names.TypeName;
import com.palantir.conjure.defs.types.reference.ReferenceType;
import com.palantir.conjure.gen.typescript.poet.ExportStatement;
import com.palantir.conjure.gen.typescript.poet.ImportStatement;
import com.palantir.conjure.gen.typescript.poet.StandardImportStatement;
import com.palantir.conjure.gen.typescript.poet.StarImportStatement;
import com.palantir.conjure.gen.typescript.poet.TypescriptSimpleType;
import com.palantir.conjure.gen.typescript.types.TypeMapper;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;

public final class GenerationUtils {

    private GenerationUtils() {}

    /**
     * Strips the first part of the package, converts the second part to the scope, and converts all remaining parts to
     * the module name by converting '.' to '-'.
     * <p>
     * For example, a package of "com.palantir.foundry.api" would produce "@palantir/foundry-api".
     */
    public static String packageToScopeAndModule(ConjurePackage packageName) {
        String[] parts = packageName.name().split("\\.");
        Preconditions.checkArgument(parts.length > 2, "packages should have at least 3 segments");

        String scope = parts[1];
        List<String> moduleParts = Lists.newArrayList(parts).subList(2, parts.length);
        String module = Joiner.on('-').join(moduleParts);

        return String.format("@%s/%s", scope, module);
    }

    public static List<ImportStatement> generateImportStatements(
            List<ConjureType> conjureTypes, TypeName sourceName, TypeMapper mapper) {
        return getReferenceTypeStream(conjureTypes, sourceName, mapper)
                .map(referenceType -> generateImportStatement(referenceType, sourceName.conjurePackage(), mapper))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private static Optional<ImportStatement> generateImportStatement(
            ReferenceType referenceType, ConjurePackage sourcePackage, TypeMapper mapper) {
        Optional<ConjurePackage> maybeDestPackage = mapper.getContainingPackage(referenceType);
        return maybeDestPackage.map(destPackage -> {
            TypescriptSimpleType typeScriptType = mapper.getTypescriptType(referenceType);
            if (Objects.equals(sourcePackage, destPackage)) {
                return StandardImportStatement.builder()
                        .addNamesToImport(typeScriptType.name())
                        .filepathToImport(getTypescriptFilePath(referenceType.type().name()))
                        .build();
            }
            return StandardImportStatement.builder()
                    .addNamesToImport(typeScriptType.name())
                    .filepathToImport(packageToScopeAndModule(destPackage))
                    .build();
        });
    }

    public static List<ImportStatement> generateStarImportStatements(List<ConjureType> conjureTypes,
            Function<TypescriptSimpleType, String> getVariableName, TypeName sourceName,
            TypeMapper mapper) {
        return getReferenceTypeStream(conjureTypes, sourceName, mapper)
                .map(referenceType -> generateStarImportStatement(
                        referenceType, getVariableName, sourceName.conjurePackage(), mapper))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private static Optional<StarImportStatement> generateStarImportStatement(
            ReferenceType referenceType, Function<TypescriptSimpleType, String> getVariableName,
            ConjurePackage sourcePackage, TypeMapper mapper) {
        Optional<ConjurePackage> maybeDestPackage = mapper.getContainingPackage(referenceType);
        return maybeDestPackage.map(destPackage -> {
            TypescriptSimpleType typeScriptType = mapper.getTypescriptType(referenceType);
            String filepath = Objects.equals(sourcePackage, destPackage)
                    ? getTypescriptFilePath(referenceType.type().name())
                    : packageToScopeAndModule(destPackage);
            return StarImportStatement.builder()
                    .variableName(getVariableName.apply(typeScriptType))
                    .filepathToImport(filepath)
                    .build();
        });
    }

    private static Stream<ReferenceType> getReferenceTypeStream(
            List<ConjureType> conjureTypes, TypeName sourceName, TypeMapper mapper) {
        return conjureTypes.stream()
                .flatMap(conjureType -> mapper.getReferencedConjureNames(conjureType).stream())
                .distinct()
                .filter(referenceType -> !referenceType.type().equals(sourceName))
                .filter(referenceType -> !mapper.getTypescriptType(referenceType).isPrimitive());
    }

    public static ExportStatement createExportStatementRelativeToRoot(String sourceName, String... exportNames) {
        String tsFilePath = getTypescriptFilePath(sourceName);
        return ExportStatement.builder()
                .addNamesToExport(exportNames)
                .filepathToExport(tsFilePath)
                .build();
    }

    public static String getTypescriptFilePath(String name) {
        return "./" + StringUtils.uncapitalize(name);
    }
}
