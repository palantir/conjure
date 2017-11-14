/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gradle;

import static com.google.common.base.Preconditions.checkState;

import com.palantir.conjure.defs.Conjure;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.gen.typescript.ConjureTypeScriptClientGenerator;
import com.palantir.conjure.gen.typescript.ExperimentalFeatures;
import com.palantir.conjure.gen.typescript.errors.ErrorGenerator;
import com.palantir.conjure.gen.typescript.services.ServiceGenerator;
import com.palantir.conjure.gen.typescript.types.TypeGenerator;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
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

    @Input
    private ErrorGenerator errorGenerator;

    @Input
    private Supplier<Set<ExperimentalFeatures>> experimentalFeatures;

    public final void setErrorGenerator(ErrorGenerator errorGenerator) {
        this.errorGenerator = errorGenerator;
    }

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

    public final void setExperimentalFeatures(Supplier<Set<ExperimentalFeatures>> experimentalFeatures) {
        this.experimentalFeatures = experimentalFeatures;
    }

    @TaskAction
    public final void compileFiles() throws IOException {
        checkState(outputDirectory.exists() || outputDirectory.mkdirs(),
                "Unable to make directory tree %s", outputDirectory);
        checkState(nodeModulesOutputDirectory.exists() || nodeModulesOutputDirectory.mkdirs(),
                "Unable to make directory tree %s", nodeModulesOutputDirectory);

        compileFiles(ConjurePlugin.excludeExternalImports(getSource().getFiles()), outputDirectory);
        GitIgnore.writeGitIgnore(outputDirectory, "*.ts\npackage.json\n");

        // make all generated code available for later compilation
        compileFiles(getSource().getFiles(), nodeModulesOutputDirectory);
    }

    private void compileFiles(Collection<File> files, File outputDir) {
        ConjureTypeScriptClientGenerator generator = new ConjureTypeScriptClientGenerator(
                serviceGenerator, typeGenerator, errorGenerator, experimentalFeatures.get());

        List<ConjureDefinition> conjureDefinitions = files.stream().map(Conjure::parse).collect(Collectors.toList());
        generator.emit(conjureDefinitions, getProject().getVersion().toString(), outputDir);
    }
}
