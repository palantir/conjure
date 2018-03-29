/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs;

import static java.util.stream.Collectors.toList;
import static org.junit.Assume.assumeTrue;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.palantir.conjure.parser.ConjureParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Parameterized.class)
public class AllSpellbookDefinitionsTest {
    private static final Logger log = LoggerFactory.getLogger(AllSpellbookDefinitionsTest.class);
    private static final Path spellbookDirectory = Paths.get("../build/spellbook");
    private final Path conjureYml;

    public AllSpellbookDefinitionsTest(Path conjureYml) {
        this.conjureYml = conjureYml;
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() throws IOException, InterruptedException {
        String token = System.getenv("GH_PULL_TOKEN");

        if (Strings.isNullOrEmpty(System.getenv("CI")) && !Strings.isNullOrEmpty(token)) {
            downloadFreshTgz(token);
        } else {
            useCheckedInTgz();
        }

        return Files.walk(spellbookDirectory)
                .filter(path -> path.toFile().isFile())
                .filter(path -> path.toString().endsWith(".yml"))
                .map(path -> new Object[] {path})
                .collect(toList());
    }

    @BeforeClass
    public static final void beforeClass() {
        SharedMetricRegistries.setDefault("conjure-test", new MetricRegistry());
    }

    @Test
    public void verify_conjure_yml_deserializes() throws Exception {
        String contents = new String(Files.readAllBytes(conjureYml), StandardCharsets.UTF_8);
        assumeTrue("file doesn't use conjure imports", !contents.contains("external-imports"));
        // temporarily disabled https://github.palantir.build/foundry/conjure/issues/933
        assumeTrue("Known issue with conjure (different param-id and param-name)",
                !conjureYml.endsWith("tileserver-api.yml"));

        try {
            Conjure.parse(ImmutableList.of(conjureYml.toFile()));
        } catch (ConjureParser.ImportNotFoundException e) {

            // deprecated, removed in 2.0: https://github.palantir.build/foundry/conjure/issues/735
            assumeTrue("We can't test external-imports unfortunately", !e.getMessage().contains("external-imports"));

            // introduced in 2.1.5: https://github.palantir.build/foundry/conjure/issues/935
            assumeTrue("Known issue with relative paths", !e.getMessage().contains("../../../spark-module-tokens"));

            throw e;
        } catch (ConjureParser.CyclicImportException e) {
            log.warn("Cyclic Conjure definition in {}", conjureYml, e);
        } catch (RuntimeException e) {
            assumeTrue("Known conjure-go incompatibility",
                    !e.getMessage().contains("(\"PodCertificatePath\"): not a valid representation: null"));
            throw e;
        }
    }

    @AfterClass
    public static final void afterClass() {
        SharedMetricRegistries.getDefault().getCounters()
                .forEach((name, value) -> System.out.printf("%s: %d%n", name, value.getCount()));

        SharedMetricRegistries.getDefault().getHistograms()
                .forEach((name, value) -> System.out.printf("%s: %s %s %s%n", name,
                        value.getSnapshot().getMin(),
                        value.getSnapshot().getMean(),
                        value.getSnapshot().getMax()));
    }

    private static void downloadFreshTgz(String token) throws IOException, InterruptedException {
        log.info("Pulling fresh spellbook using $GH_PULL_TOKEN, commit this to ensure reproducible builds!");
        Process start = new ProcessBuilder()
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .command("../download-spellbook-definitions.js", token, spellbookDirectory.toString())
                .start();
        start.waitFor(5, TimeUnit.MINUTES);
        assumeTrue("Successfully downloaded spellbook definitions", start.exitValue() == 0);

        assumeTrue("tar succeeded", new ProcessBuilder()
                .directory(spellbookDirectory.getParent().toFile())
                .command("tar", "-cvzf", "../spellbook.tgz", "spellbook")
                .start()
                .waitFor(5, TimeUnit.SECONDS));
    }

    private static void useCheckedInTgz() throws InterruptedException, IOException {
        log.info("Using checked-in spellbook.tgz - set $GH_PULL_TOKEN to re-download");
        Files.createDirectories(spellbookDirectory);
        assumeTrue("expand tar succeeded", new ProcessBuilder()
                .directory(spellbookDirectory.getParent().toFile())
                .command("tar", "-xzvf", "../spellbook.tgz")
                .start()
                .waitFor(5, TimeUnit.SECONDS));
    }
}
