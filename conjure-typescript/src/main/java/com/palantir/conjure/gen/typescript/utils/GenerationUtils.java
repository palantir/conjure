/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.utils;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import com.palantir.conjure.defs.types.ConjureType;
import com.palantir.conjure.gen.typescript.poet.ExportStatement;
import com.palantir.conjure.gen.typescript.poet.ImportStatement;
import com.palantir.conjure.gen.typescript.poet.TypescriptType;
import com.palantir.conjure.gen.typescript.types.TypeMapper;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public final class GenerationUtils {

    private static final Pattern PACKAGE_BASE = Pattern.compile("^[^.]+\\.[^.]+\\.");
    private static final Pattern PACKAGE = Pattern.compile(PACKAGE_BASE.pattern() + "[^.]+.*");

    private GenerationUtils() {}

    public static String packageNameToFolderPath(String packageName) {
        Preconditions.checkArgument(PACKAGE.matcher(packageName).matches(), "packages should have at least 3 segments");
        return PACKAGE_BASE.matcher(packageName).replaceAll("").replace(".", "/");
    }

    @VisibleForTesting
    static String getRelativePath(String srcPath, String destPath) {
        String[] srcSegments = srcPath.split("/");
        String[] destSegments = destPath.split("/");
        int sharedPrefixLength = 0;
        for (; sharedPrefixLength < Math.min(srcSegments.length, destSegments.length); sharedPrefixLength++) {
            if (!srcSegments[sharedPrefixLength].equals(destSegments[sharedPrefixLength])) {
                break;
            }
        }
        int levelsUp = (srcSegments.length - 1) - sharedPrefixLength;
        StringBuilder result = new StringBuilder();
        if (levelsUp == 0) {
            result.append("./");
        } else {
            for (int j = levelsUp; j > 0; j--) {
                result.append("../");
            }
        }
        for (int i = sharedPrefixLength; i < destSegments.length; i++) {
            result.append(destSegments[i]);
            if (i != destSegments.length - 1) {
                result.append("/");
            }
        }
        return result.toString();
    }

    public static ImportStatement createImportStatement(TypescriptType typescriptType, String sourcePath,
            String destPath) {
        return createImportStatement(ImmutableSet.of(typescriptType), sourcePath, destPath);
    }

    public static ImportStatement createImportStatement(Set<TypescriptType> typescriptType, String sourcePath,
            String destPath) {
        return ImportStatement.builder().filepathToImport(getRelativePath(sourcePath, destPath)).addAllNamesToImport(
                typescriptType.stream().map(type -> type.name()).collect(Collectors.toList())).build();

    }

    public static List<ImportStatement> generateImportStatements(List<ConjureType> conjureTypes,
            String sourceName, String sourcePackage, TypeMapper mapper) {
        String folderLocation = GenerationUtils.packageNameToFolderPath(sourcePackage);
        return conjureTypes.stream()
                .flatMap(conjureType -> mapper.getReferencedConjureNames(conjureType).stream())
                .distinct()
                .filter(referenceType -> !referenceType.type().equals(sourceName))
                .map(referenceType -> {
                    Optional<String> maybeDestName = mapper.getContainingPackage(referenceType);
                    return maybeDestName.map(destName -> {
                        String destFolder = GenerationUtils.packageNameToFolderPath(destName);
                        return GenerationUtils.createImportStatement(mapper.getTypescriptType(referenceType),
                                getTypescriptFilePath(folderLocation, sourceName),
                                getTypescriptFilePath(destFolder, referenceType.type()));
                    });
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public static ExportStatement createExportStatementRelativeToRoot(
            String exportName, String parentFolderPath, String sourceName) {
        String tsFilePath = getTypescriptFilePath(parentFolderPath, sourceName);
        List<String> segments = ImmutableList.copyOf(tsFilePath.split("/"));
        String filepathToExport = "./" + Joiner.on("/").join(segments);

        return ExportStatement.builder()
                .addNamesToExport(exportName)
                .filepathToExport(filepathToExport)
                .build();
    }

    public static String getCharSource(File file) throws IOException {
        return Files.asCharSource(file, StandardCharsets.UTF_8).read();
    }

    public static String getTypescriptFilePath(String parentFolder, String name) {
        return parentFolder + "/" + StringUtils.uncapitalize(name);
    }
}
