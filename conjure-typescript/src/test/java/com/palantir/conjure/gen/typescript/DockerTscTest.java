/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript;

import com.google.common.base.Stopwatch;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.io.ByteStreams;
import com.palantir.conjure.ConjureSubfolderRunner;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
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
        log.info("Checking {}", directory);
        Stopwatch started = Stopwatch.createStarted();

        Path inputDirectory = directory.resolve("expected");
        if (!inputDirectory.toFile().exists() || inputDirectory.toFile().listFiles().length == 0) {
            log.info("Skipping empty {}", inputDirectory);
            return;
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
        runProcess(command);
        log.info("finished {} {}s", directory, started.elapsed(TimeUnit.SECONDS));
    }

    /**
     * Returns the SHA hash of the freshly built docker image, suitable for `docker run $SHA`
     */
    private static String dockerBuild(String tag, String dockerfile) {
        String processOutput = runProcess("docker", "build",
                Paths.get(dockerfile).getParent().toAbsolutePath().toString());
        log.info("{}", processOutput);

        // docker build is a noisy process, but we know the last line will contain the SHA we want
        // e.g. 'Successfully built 6c0ec38cfd05'
        String[] lines = processOutput.split("\n");
        String lastLine = lines[lines.length - 1];
        String sha = lastLine.replace("Successfully built ", "");
        return sha;
    }

    private static String mountPath(Path hostPath, String dockerPath) throws IOException {
        return hostPath.toFile().getCanonicalFile().getAbsolutePath() + ":" + dockerPath;
    }

    private static String runProcess(String... args) {
        try {
            log.info("{}", Arrays.stream(args).collect(Collectors.joining(" ")));
            Process process = new ProcessBuilder()
                    .command(args)
                    .redirectError(ProcessBuilder.Redirect.INHERIT)
                    .redirectOutput(ProcessBuilder.Redirect.PIPE)
                    .start();

            process.waitFor(1, TimeUnit.MINUTES);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ByteStreams.copy(process.getInputStream(), outputStream);
            String standardOut = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);

            if (process.exitValue() != 0) {
                throw new RuntimeException(String.format(
                        "Non-zero exit (%s): %s", process.exitValue(), standardOut));
            }

            return standardOut;
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
