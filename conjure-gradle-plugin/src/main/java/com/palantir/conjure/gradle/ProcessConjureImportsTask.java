/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gradle;

import java.io.File;
import java.util.function.Supplier;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.TaskAction;

public class ProcessConjureImportsTask extends SourceTask {

    private Supplier<FileCollection> conjureImports;

    public final void setConjureImports(Supplier<FileCollection> conjureImports) {
        this.conjureImports = conjureImports;
    }

    @Input
    public final FileCollection getConjureImports() {
        return conjureImports.get();
    }

    @TaskAction
    public final void processImports() {
        Project project = getProject();

        File baseDir = new File(project.getBuildDir(), "conjure");

        project.copy(copySpec -> copySpec.into(project.file(new File(baseDir, "external-imports")))
                .from(getConjureImports()));
        project.copy(copySpec -> copySpec.into(project.file(baseDir))
                .from(getSource()));
    }

}
