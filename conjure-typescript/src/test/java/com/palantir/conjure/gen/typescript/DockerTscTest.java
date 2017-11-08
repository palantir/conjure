/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript;

import com.google.common.base.Stopwatch;
import com.google.common.io.ByteStreams;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.assertj.core.api.SoftAssertions;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Spins up the tsc compiler in a docker container to attempt to compile every checked in 'expected' folder referenced
 * by TypescriptGeneratorTest.
 */
@SuppressWarnings("Slf4jLogsafeArgs")
public class DockerTscTest {

    private static final Logger log = LoggerFactory.getLogger(DockerTscTest.class);
    private static final Path tsconfig = Paths.get(
            "../conjure-publish-gradle-plugin/src/main/resources/tsconfig.json");
    private static final Path feLib = Paths.get(
            "../conjure-publish-gradle-plugin/src/main/resources/conjure-fe-lib_index.d.ts");

    @BeforeClass
    public static void beforeClass() throws IOException, InterruptedException {
        log.info("{}", dockerBuild("conjure/tsc", "src/test/resources/Dockerfile"));
    }

    @Test
    public void compileAll() throws Exception {
        File otherResources = new File("src/test/resources");
        List<File> directories = Arrays.stream(otherResources.listFiles())
                .filter(File::isDirectory)
                .collect(Collectors.toList());

        SoftAssertions softly = new SoftAssertions();
        directories.parallelStream().forEach(directory -> {
            String errors = dockerTsc(directory);
            softly.assertThat(errors)
                    .as("tsc for " + directory)
                    .isEmpty();
        });
        softly.assertAll();
    }

    private String dockerTsc(File directory) {
        log.info("Checking {}", directory);
        Stopwatch started = Stopwatch.createStarted();

        Path inputDirectory = directory.toPath().resolve("expected");
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
                "conjure/tsc"
        };
        String tscOutput = runProcess(command);
        log.info("finished {} {}s", directory, started.elapsed(TimeUnit.SECONDS));
        return tscOutput;
    }

    private static String dockerBuild(String tag, String dockerfile) throws IOException {
        return runProcess("docker", "build",
                Paths.get(dockerfile).getParent().toAbsolutePath().toString(),
                "--tag", tag);
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
