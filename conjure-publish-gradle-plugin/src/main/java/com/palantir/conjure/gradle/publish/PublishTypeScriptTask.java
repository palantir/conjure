/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gradle.publish;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.ws.rs.core.HttpHeaders;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.io.FileUtils;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.TaskAction;

public class PublishTypeScriptTask extends ConventionTask {

    private File inputDirectory;

    public final void setInputDirectory(File inputDirectory) {
        this.inputDirectory = inputDirectory;
    }

    @InputDirectory
    public final File getInputDirectory() {
        return inputDirectory;
    }

    @TaskAction
    public final void publish() {
        File publishWorkingDirectory = new File(getProject().getBuildDir(), "publishWorkingDirectory");
        try {
            if (!publishWorkingDirectory.exists()) {
                FileUtils.forceMkdir(publishWorkingDirectory);
            }
            ConjurePublishPlugin.copyDirectory(getInputDirectory(), publishWorkingDirectory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Fetch npm token for each scope
        OkHttpClient client = new OkHttpClient.Builder().build();
        for (File scopeDir : publishWorkingDirectory.listFiles()) {
            String artifactoryCreds = getArtifactoryCreds(client, scopeDir.getName());
            // Publish each package
            for (File packageDir : scopeDir.listFiles()) {
                createNpmrcFileAndPublish(packageDir, artifactoryCreds);
            }
        }

    }

    private String getArtifactoryCreds(OkHttpClient client, String scope) {
        String artifactoryUsername = System.getenv("ARTIFACTORY_USERNAME");
        String artifactoryPassword = System.getenv("ARTIFACTORY_PASSWORD");
        String userPass = artifactoryUsername + ":" + artifactoryPassword;
        String base64UserPass = Base64.getEncoder().encodeToString(userPass.getBytes(StandardCharsets.UTF_8));
        Request request = new Request.Builder()
                .url("https://artifactory.palantir.build/artifactory/api/npm/all-npm/auth/" + scope)
                .header(HttpHeaders.AUTHORIZATION, "Basic " + base64UserPass)
                .build();
        try {
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createNpmrcFileAndPublish(File packageDir, String artifactoryCreds) {
        File npmrcFile = new File(packageDir, ".npmrc");
        ConjurePublishPlugin.makeFile(npmrcFile, artifactoryCreds);
        // npm publish will ignore .npmrc unless it has the right permissions (because it contains secrets)
        npmrcFile.setReadable(true, false);

        // Run npm publish
        getProject().exec(execSpec -> {
            execSpec.setWorkingDir(packageDir);
            execSpec.commandLine("npm", "publish", "./dist");
        });

    }

}
