/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gradle;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.palantir.conjure.defs.Conjure;
import com.palantir.conjure.spec.ConjureDefinition;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.TaskAction;

public class CompileIrTask extends SourceTask {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new Jdk8Module())
            .setSerializationInclusion(JsonInclude.Include.NON_ABSENT);

    private File outputFile;

    public final void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    @OutputFile
    public final File getOutputFile() {
        return outputFile;
    }

    @TaskAction
    public final void generate() throws IOException {
        writeToIr(getSource().getFiles(), outputFile);
    }

    protected static void writeToIr(Set<File> sourceFiles, File destination) throws IOException {
        ConjureDefinition conjureDefinition = Conjure.parse(sourceFiles);
        OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValue(destination, conjureDefinition);
    }
}
