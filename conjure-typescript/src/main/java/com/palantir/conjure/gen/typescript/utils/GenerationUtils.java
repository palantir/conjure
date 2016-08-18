/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.utils;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.palantir.conjure.defs.types.ConjureType;
import com.palantir.conjure.gen.typescript.poet.ImportStatement;
import com.palantir.conjure.gen.typescript.poet.TypescriptType;
import com.palantir.conjure.gen.typescript.types.TypeMapper;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public final class GenerationUtils {

    private GenerationUtils() {
        // No
    }

    public static String packageNameToFolderPath(String packageName) {
        return packageName.replace(".", "/");
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

    public static ImportStatement createImportStatement(TypescriptType typescriptType, String currentPackage,
            String currentName, String packageName, String name) {
        return createImportStatement(Sets.newHashSet(typescriptType), currentPackage, currentName, packageName, name);
    }

    public static ImportStatement createImportStatement(Set<TypescriptType> typescriptType, String currentPackage,
            String currentName, String packageName, String name) {
        String sourcePath =  packageNameToFolderPath(currentPackage) + "/" + StringUtils.uncapitalize(currentName);
        String destpath = packageNameToFolderPath(packageName) + "/" + StringUtils.uncapitalize(name);
        return ImportStatement.builder().filepathToImport(getRelativePath(sourcePath, destpath)).addAllNamesToImport(
                typescriptType.stream().map(type -> type.name()).collect(Collectors.toList())).build();

    }

    public static List<ImportStatement> generateImportStatements(List<ConjureType> conjureTypes,
            String currentName, String packageLocation, TypeMapper mapper) {
        return conjureTypes.stream()
                .flatMap(conjureType -> mapper.getReferencedConjureNames(conjureType).stream())
                .distinct()
                .filter(conjureType -> !conjureType.equals(currentName))
                .map(conjureType -> {
                    String packageName = mapper.getContainingPackage(conjureType);
                    if (packageName != null) {
                        return GenerationUtils.createImportStatement(mapper.getTypescriptType(conjureType),
                                packageLocation, currentName, packageName, conjureType.type());
                    } else {
                        return null;
                    }
                })
                .filter(conjureType -> conjureType != null)
                .collect(Collectors.toList());
    }

    public static String getCharSource(File file) throws IOException {
        return Files.asCharSource(file, StandardCharsets.UTF_8).read();
    }
}
