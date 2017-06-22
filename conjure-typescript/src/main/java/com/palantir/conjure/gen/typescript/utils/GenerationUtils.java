/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.utils;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.palantir.conjure.defs.types.ConjureType;
import com.palantir.conjure.defs.types.names.ConjurePackage;
import com.palantir.conjure.defs.types.names.TypeName;
import com.palantir.conjure.defs.types.reference.ReferenceType;
import com.palantir.conjure.gen.typescript.poet.ExportStatement;
import com.palantir.conjure.gen.typescript.poet.ImportStatement;
import com.palantir.conjure.gen.typescript.poet.TypescriptSimpleType;
import com.palantir.conjure.gen.typescript.types.TypeMapper;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
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

    public static List<ImportStatement> generateImportStatements(List<ConjureType> conjureTypes,
            TypeName sourceName, ConjurePackage sourcePackage, TypeMapper mapper) {
        return conjureTypes.stream()
                .flatMap(conjureType -> mapper.getReferencedConjureNames(conjureType).stream())
                .distinct()
                .filter(referenceType -> !referenceType.type().equals(sourceName))
                .filter(referenceType -> !mapper.getTypescriptType(referenceType).isPrimitive())
                .map(referenceType -> generateImportStatement(referenceType, sourceName, sourcePackage, mapper))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private static Optional<ImportStatement> generateImportStatement(
            ReferenceType referenceType, TypeName sourceName, ConjurePackage sourcePackage, TypeMapper mapper) {
        Optional<ConjurePackage> maybeDestPackage = mapper.getContainingPackage(referenceType);
        return maybeDestPackage.map(destPackage -> {
            TypescriptSimpleType typeScriptType = mapper.getTypescriptType(referenceType);
            if (Objects.equals(sourcePackage, destPackage)) {
                return ImportStatement.builder()
                        .addNamesToImport(typeScriptType.name())
                        .filepathToImport(getTypescriptFilePath(referenceType.type().name()))
                        .build();
            }
            return ImportStatement.builder()
                    .addNamesToImport(typeScriptType.name())
                    .filepathToImport(packageToScopeAndModule(destPackage))
                    .build();
        });
    }

    public static ExportStatement createExportStatementRelativeToRoot(String exportName, String sourceName) {
        String tsFilePath = getTypescriptFilePath(sourceName);
        return ExportStatement.builder()
                .addNamesToExport(exportName)
                .filepathToExport(tsFilePath)
                .build();
    }

    public static String getCharSource(File file) throws IOException {
        return Files.asCharSource(file, StandardCharsets.UTF_8).read();
    }

    public static String getTypescriptFilePath(String name) {
        return "./" + StringUtils.uncapitalize(name);
    }
}
