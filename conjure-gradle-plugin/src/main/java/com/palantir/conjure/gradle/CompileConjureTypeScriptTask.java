/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gradle;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.io.Files;
import com.palantir.conjure.defs.Conjure;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.gen.typescript.ConjureTypeScriptClientGenerator;
import com.palantir.conjure.gen.typescript.services.ServiceGenerator;
import com.palantir.conjure.gen.typescript.types.TypeGenerator;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.TaskAction;

public class CompileConjureTypeScriptTask extends SourceTask {

    @OutputDirectory
    private File outputDirectory;

    @OutputDirectory
    private File nodeModulesOutputDirectory;

    @Input
    private ServiceGenerator serviceGenerator;

    @Input
    private TypeGenerator typeGenerator;

    public final void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public final void setNodeModulesOutputDirectory(File nodeModulesOutputDirectory) {
        this.nodeModulesOutputDirectory = nodeModulesOutputDirectory;
    }

    public final void setServiceGenerator(ServiceGenerator serviceGenerator) {
        this.serviceGenerator = serviceGenerator;
    }

    public final void setTypeGenerator(TypeGenerator typeGenerator) {
        this.typeGenerator = typeGenerator;
    }

    @TaskAction
    public final void compileFiles() throws IOException {
        checkState(outputDirectory.exists() || outputDirectory.mkdirs(),
                "Unable to make directory tree %s", outputDirectory);
        checkState(nodeModulesOutputDirectory.exists() || nodeModulesOutputDirectory.mkdirs(),
                "Unable to make directory tree %s", nodeModulesOutputDirectory);

        compileFiles(ConjurePlugin.excludeExternalImports(getSource().getFiles()), outputDirectory);
        // write a gitignore to prevent the generated files ending up in source control
        Files.write("*.ts\npackage.json\n", new File(outputDirectory, ".gitignore"), StandardCharsets.UTF_8);

        // make all generated code available for later compilation
        compileFiles(getSource().getFiles(), nodeModulesOutputDirectory);
    }

    private void compileFiles(Collection<File> files, File outputDir) {
        ConjureTypeScriptClientGenerator generator = new ConjureTypeScriptClientGenerator(
                serviceGenerator, typeGenerator);

        List<ConjureDefinition> conjureDefinitions = files.stream().map(Conjure::parse).collect(Collectors.toList());
        generator.emit(conjureDefinitions, getProject().getVersion().toString(), outputDir);
    }

}
