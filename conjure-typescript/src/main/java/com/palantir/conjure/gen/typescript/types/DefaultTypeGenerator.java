/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.types;

import com.palantir.conjure.defs.TypesDefinition;
import com.palantir.conjure.defs.types.BaseObjectTypeDefinition;
import com.palantir.conjure.defs.types.ConjureType;
import com.palantir.conjure.defs.types.EnumTypeDefinition;
import com.palantir.conjure.defs.types.ObjectTypeDefinition;
import com.palantir.conjure.defs.types.OptionalType;
import com.palantir.conjure.gen.typescript.poet.ImportStatement;
import com.palantir.conjure.gen.typescript.poet.TypescriptFile;
import com.palantir.conjure.gen.typescript.poet.TypescriptInterface;
import com.palantir.conjure.gen.typescript.poet.TypescriptTypeSignature;
import com.palantir.conjure.gen.typescript.utils.GenerationUtils;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public final class DefaultTypeGenerator implements TypeGenerator {

    @Override
    public TypescriptFile generateType(TypesDefinition types, String defaultPackage, String typeName,
            BaseObjectTypeDefinition baseTypeDef) {
        if (baseTypeDef instanceof EnumTypeDefinition) {
            // TODO support enums
            throw new RuntimeException("Not supported");
        }
        ObjectTypeDefinition typeDef = (ObjectTypeDefinition) baseTypeDef;

        String packageLocation = typeDef.packageName().orElse(defaultPackage);
        TypeMapper mapper = new TypeMapper(types, defaultPackage);
        String parentFolderPath = GenerationUtils.packageNameToFolderPath(packageLocation);
        Set<TypescriptTypeSignature> propertySignatures = typeDef.fields().entrySet()
                .stream()
                .map(e -> (TypescriptTypeSignature) TypescriptTypeSignature.builder()
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

}
