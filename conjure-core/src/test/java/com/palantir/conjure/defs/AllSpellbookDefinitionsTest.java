/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs;

import static java.util.stream.Collectors.toList;
import static org.junit.Assume.assumeTrue;

import com.google.common.base.Strings;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
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

    @Test
    public void verify_conjure_yml_deserializes() throws Exception {
        String contents = new String(Files.readAllBytes(conjureYml), StandardCharsets.UTF_8);
        assumeTrue("file doesn't use conjure imports", !contents.contains("external-imports"));

        Conjure.parse(conjureYml.toFile());
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
