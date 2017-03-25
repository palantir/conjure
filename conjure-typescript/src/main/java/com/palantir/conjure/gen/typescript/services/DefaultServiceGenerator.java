/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.services;

import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.defs.ConjureImports;
import com.palantir.conjure.gen.typescript.poet.ExportStatement;
import com.palantir.conjure.gen.typescript.poet.TypescriptFile;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class DefaultServiceGenerator implements ServiceGenerator {

    private final InterfaceServiceGenerator interfaceDelegate;
    private final ClassServiceGenerator classDelegate;

    public DefaultServiceGenerator() {
        this.interfaceDelegate = new InterfaceServiceGenerator();
        this.classDelegate = new ClassServiceGenerator();
    }

    @Override
    public Set<TypescriptFile> generate(ConjureDefinition conjureDefinition, ConjureImports imports) {
        return Stream.concat(interfaceDelegate.generate(conjureDefinition, imports).stream(),
                classDelegate.generate(conjureDefinition, imports).stream()).collect(Collectors.toSet());
    }

    @Override
    public Set<ExportStatement> generateExports(ConjureDefinition conjureDefinition) {
        return Stream.concat(interfaceDelegate.generateExports(conjureDefinition).stream(),
                classDelegate.generateExports(conjureDefinition).stream()).collect(Collectors.toSet());
    }
}
