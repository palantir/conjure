/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript;

import com.google.common.base.Stopwatch;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.io.ByteStreams;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.SoftAssertions;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Spins up the tsc compiler in a docker container to attempt to compile every checked in 'expected' folder referenced
 * by TypescriptGeneratorTest.
 */
@ConjureSubfolderRunner.ParentFolder(value = "src/test/resources", parallel = true)
@RunWith(ConjureSubfolderRunner.class)
@SuppressWarnings("Slf4jLogsafeArgs")
public final class DockerTscTest {

    private static final Logger log = LoggerFactory.getLogger(DockerTscTest.class);
    private static final Path tsconfig = Paths.get(
            "../conjure-publish-gradle-plugin/src/main/resources/tsconfig.json");
    private static final Path feLib = Paths.get(
            "../conjure-publish-gradle-plugin/src/main/resources/conjure-fe-lib_index.d.ts");
    private static final Supplier<String> dockerTag = Suppliers.memoize(() ->
            dockerBuild("conjure/tsc", "src/test/resources/Dockerfile"));

    @ConjureSubfolderRunner.Test
    public void assertThatGeneratedCodeCompilesWithTsc(Path directory) throws Exception {
        SoftAssertions softly = new SoftAssertions();
        String errors = dockerTsc(directory);
        softly.assertThat(errors)
                .as("tsc for " + directory)
                .isEmpty();
        softly.assertAll();
    }

    private String dockerTsc(Path directory) {
        log.info("Checking {}", directory);
        Stopwatch started = Stopwatch.createStarted();

        Path inputDirectory = directory.resolve("expected");
        if (!inputDirectory.toFile().exists() || inputDirectory.toFile().listFiles().length == 0) {
            log.info("Skipping empty {}", inputDirectory);
            return "";
        }

        String[] command = {
                "docker", "run",
                // mount in the source files ts files as if a human had written them
                "-v", mountPath(inputDirectory, "/workdir/src"),
                // also bootstrap the node_modules dir with the same TS source files so that cross-imports work
                "-v", mountPath(inputDirectory, "/workdir/node_modules"),
                // typescript compiler requires a tsconfig.json
                "-v", mountPath(tsconfig, "/workdir/tsconfig.json"),
                // fudge the conjure-fe-lib typings to that we don't make network calls
                // (different node_modules dir ensures docker mounting doesn't freak out)
                "-v", mountPath(feLib, "/node_modules/@foundry/conjure-fe-lib/index.d.ts"),
                "--rm",
                dockerTag.get()
        };
        String tscOutput = runProcess(command);
        log.info("finished {} {}s", directory, started.elapsed(TimeUnit.SECONDS));
        return tscOutput;
    }

    private static String dockerBuild(String tag, String dockerfile) {
        log.info("{}", runProcess("docker", "build",
                Paths.get(dockerfile).getParent().toAbsolutePath().toString(),
                "--tag", tag));
        return "conjure/tsc";
    }

    private static String mountPath(Path hostPath, String dockerPath) {
        try {
            return hostPath.toFile().getCanonicalFile().getAbsolutePath() + ":" + dockerPath;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String runProcess(String... args) {
        try {
            Process process = new ProcessBuilder()
                    .command(args)
                    .redirectError(ProcessBuilder.Redirect.INHERIT)
                    .redirectOutput(ProcessBuilder.Redirect.PIPE)
                    .start();

            process.waitFor(20, TimeUnit.SECONDS);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ByteStreams.copy(process.getInputStream(), outputStream);
            return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
