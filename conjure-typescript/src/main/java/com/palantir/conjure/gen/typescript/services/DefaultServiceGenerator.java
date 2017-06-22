/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.services;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.defs.types.names.ConjurePackage;
import com.palantir.conjure.gen.typescript.poet.ExportStatement;
import com.palantir.conjure.gen.typescript.poet.TypescriptFile;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class DefaultServiceGenerator implements ServiceGenerator {

    private final InterfaceServiceGenerator interfaceGeneratorDelegate;
    private final ClassServiceGenerator classGeneratorDelegate;

    public DefaultServiceGenerator() {
        this.interfaceGeneratorDelegate = new InterfaceServiceGenerator();
        this.classGeneratorDelegate = new ClassServiceGenerator();
    }

    @Override
    public Set<TypescriptFile> generate(ConjureDefinition conjureDefinition) {
        return Stream.concat(interfaceGeneratorDelegate.generate(conjureDefinition).stream(),
                classGeneratorDelegate.generate(conjureDefinition).stream()).collect(Collectors.toSet());
    }

    @Override
    public Map<ConjurePackage, Collection<ExportStatement>> generateExports(ConjureDefinition conjureDefinition) {
        Multimap<ConjurePackage, ExportStatement> classAndInterfaceExports = HashMultimap.create();
        interfaceGeneratorDelegate.generateExports(conjureDefinition).forEach(
                (conjurePackage, exports) -> classAndInterfaceExports.putAll(conjurePackage, exports));
        classGeneratorDelegate.generateExports(conjureDefinition).forEach(
                (conjurePackage, exports) -> classAndInterfaceExports.putAll(conjurePackage, exports));
        return classAndInterfaceExports.asMap();
    }
}
