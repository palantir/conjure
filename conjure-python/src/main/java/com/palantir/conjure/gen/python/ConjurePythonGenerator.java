/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.python;

import com.google.common.collect.Streams;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.defs.ConjureImports;
import com.palantir.conjure.defs.TypesDefinition;
import com.palantir.conjure.defs.types.BaseObjectTypeDefinition;
import com.palantir.conjure.gen.python.client.ClientGenerator;
import com.palantir.conjure.gen.python.poet.PythonClass;
import com.palantir.conjure.gen.python.poet.PythonFile;
import com.palantir.conjure.gen.python.types.BeanGenerator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class ConjurePythonGenerator {

    private final BeanGenerator beanGenerator;
    private final ClientGenerator clientGenerator;

    public ConjurePythonGenerator(BeanGenerator beanGenerator, ClientGenerator clientGenerator) {
        this.beanGenerator = beanGenerator;
        this.clientGenerator = clientGenerator;
    }

    public void write(ConjureDefinition conjureDefinition, ConjureImports importedTypes, PythonFileWriter writer) {
        generate(conjureDefinition, importedTypes).forEach(writer::writePythonFile);
    }

    public List<PythonFile> generate(ConjureDefinition conjureDefinition, ConjureImports importedTypes) {
        TypesDefinition types = conjureDefinition.types();

        PackageNameProcessor packageNameProcessor = new TwoComponentStrippingPackageNameProcessor(
                new DefaultPackageNameProcessor(conjureDefinition.types().definitions().defaultConjurePackage()));

        List<PythonClass> beanClasses = types.definitions()
                .objects()
                .entrySet()
                .stream()
                .map(entry -> {
                    String name = entry.getKey();
                    BaseObjectTypeDefinition objectDefinition = entry.getValue();
                    return beanGenerator.generateObject(
                            types,
                            importedTypes,
                            packageNameProcessor,
                            name,
                            objectDefinition);
                })
                .collect(Collectors.toList());

        List<PythonClass> serviceClasses = conjureDefinition.services()
                .entrySet()
                .stream()
                .map(entry -> clientGenerator.generateClient(
                        types, importedTypes, packageNameProcessor, entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        Map<String, List<PythonClass>> classesByPackageName =
                Streams.concat(beanClasses.stream(), serviceClasses.stream())
                    .collect(Collectors.groupingBy(PythonClass::packageName));

        // group into files
        return classesByPackageName.entrySet()
                .stream()
                .map(entry -> PythonFile.builder()
                        .packageName(entry.getKey())
                        .addAllImports(entry.getValue()
                                .stream()
                                .flatMap(pt -> pt.requiredImports().stream())
                                .collect(Collectors.toSet()))
                        .addAllContents(entry.getValue())
                        .build())
                .collect(Collectors.toList());
    }

}
