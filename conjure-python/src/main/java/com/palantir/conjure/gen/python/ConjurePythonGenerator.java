/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.python;

import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.defs.types.TypeDefinition;
import com.palantir.conjure.gen.python.client.ClientGenerator;
import com.palantir.conjure.gen.python.poet.PythonClass;
import com.palantir.conjure.gen.python.poet.PythonFile;
import com.palantir.conjure.gen.python.types.PythonBeanGenerator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ConjurePythonGenerator {

    private final PythonBeanGenerator beanGenerator;
    private final ClientGenerator clientGenerator;

    public ConjurePythonGenerator(PythonBeanGenerator beanGenerator, ClientGenerator clientGenerator) {
        this.beanGenerator = beanGenerator;
        this.clientGenerator = clientGenerator;
    }

    public void write(ConjureDefinition conjureDefinition, PythonFileWriter writer) {
        generate(conjureDefinition).forEach(writer::writePythonFile);
    }

    public List<PythonFile> generate(ConjureDefinition conjureDefinition) {
        List<TypeDefinition> types = conjureDefinition.types();

        PackageNameProcessor packageNameProcessor = new TwoComponentStrippingPackageNameProcessor();
        List<PythonClass> beanClasses = types
                .stream()
                .map(objectDefinition -> beanGenerator.generateObject(types, packageNameProcessor, objectDefinition))
                .collect(Collectors.toList());

        List<PythonClass> serviceClasses = conjureDefinition.services()
                .stream()
                .map(serviceDef -> clientGenerator.generateClient(types, packageNameProcessor, serviceDef))
                .collect(Collectors.toList());

        Map<String, List<PythonClass>> classesByPackageName =
                Stream.concat(beanClasses.stream(), serviceClasses.stream())
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
