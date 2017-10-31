/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gradle.publish;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import javax.ws.rs.core.HttpHeaders;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.io.FileUtils;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

public class GenerateNpmrcTask extends ConventionTask {

    public static final String NPMRC_FILENAME = ".npmrc";

    @InputDirectory
    private File inputDirectory;

    @OutputDirectory
    private File outputDirectory;

    public final void setInputDirectory(File inputDirectory) {
        this.inputDirectory = inputDirectory;
    }

    public final void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    @TaskAction
    public final void generateNpmrc() {
        try {
            if (!outputDirectory.exists()) {
                FileUtils.forceMkdir(outputDirectory);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Fetch npm token for each scope
        OkHttpClient client = new OkHttpClient.Builder().build();
        for (File scopeDir : inputDirectory.listFiles()) {
            String artifactoryCreds = getArtifactoryCreds(client, scopeDir.getName());
            if (scopeDir.isDirectory()) {
                for (File packageDir : scopeDir.listFiles()) {
                    createNpmrcFile(scopeDir.getName(), packageDir.getName(), artifactoryCreds);
                }
            }
        }
    }

    private String getArtifactoryCreds(OkHttpClient client, String scope) {
        String artifactoryUsername = System.getenv("ARTIFACTORY_USERNAME");
        String artifactoryPassword = System.getenv("ARTIFACTORY_PASSWORD");
        String userPass = String.format("%s:%s", artifactoryUsername, artifactoryPassword);
        String base64UserPass = Base64.getEncoder().encodeToString(userPass.getBytes(StandardCharsets.UTF_8));
        String trimmedScope = scope.startsWith("@") ? scope.substring(1) : scope;
        String npmRegistryUriWithScope = String.format("%s/auth/%s", getNpmRegistryUri(), trimmedScope);
        Request request = new Request.Builder()
                .url(npmRegistryUriWithScope)
                .header(HttpHeaders.AUTHORIZATION, "Basic " + base64UserPass)
                .build();
        try {
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getNpmRegistryUri() {
        return getProject().hasProperty("npmRegistryUri")
                ? getProject().property("npmRegistryUri").toString()
                : "https://artifactory.palantir.build/artifactory/api/npm/all-npm";
    }

    private void createNpmrcFile(String scopeDirName, String packageDirName, String artifactoryCreds) {
        Path npmrcFile = Paths.get(outputDirectory.getAbsolutePath(), scopeDirName, packageDirName, NPMRC_FILENAME);
        ConjurePublishPlugin.makeFile(npmrcFile.toFile(), artifactoryCreds);
    }

}
