/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java;


import com.google.common.collect.ImmutableMap;
import com.palantir.conjure.defs.Conjure;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.defs.types.reference.ConjureImports;
import com.palantir.conjure.gen.java.services.JerseyServiceGenerator;
import com.palantir.conjure.gen.java.types.BeanGenerator;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public final class ConjureYmlParsingTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Parameterized.Parameters(name = "{0} is valid Conjure YML: {1}")
    public static Collection<Object[]> data() {
        List<Object[]> objects = new ArrayList<>();

        // add all valid test cases
        objects.addAll(getTestFiles(new File("src/test/resources/testymls/valid"), true));

        // add all invalid test cases
        objects.addAll(getTestFiles(new File("src/test/resources/testymls/invalid"), false));

        return objects;
    }

    private static List<Object[]> getTestFiles(File dir, boolean isValid) {
        List<Object[]> objects = new ArrayList<>();
        File[] files = dir.listFiles();
        Arrays.sort(files);
        for (File f : files) {
            objects.add(new Object[] {f, isValid});
        }
        return objects;
    }

    private final File file;
    private final boolean valid;

    public ConjureYmlParsingTest(File file, boolean valid) {
        this.file = file;
        this.valid = valid;
    }

    @Test
    public void testValid() throws IOException {
        boolean exceptionThrown = false;
        try {
            ConjureDefinition conjure = Conjure.parse(file);
            ConjureImports imports = new ConjureImports(ImmutableMap.of());
            File src = folder.newFolder("src");
            Settings settings = Settings.standard();
            ConjureJavaServiceAndTypeGenerator generator = new ConjureJavaServiceAndTypeGenerator(
                    new JerseyServiceGenerator(),
                    new BeanGenerator(settings));
            generator.emit(conjure, imports, src);
        } catch (RuntimeException e) {
            exceptionThrown = true;
            if (valid) {
                // if test case is supposed to be a valid YML file, re-throw exception
                throw e;
            }
        }

        if (!valid && !exceptionThrown) {
            // if test case is supposed to be invalid YML file and no exception was throw, fail
            Assert.fail("parsing Conjure YML and emitting generated files did not cause any failures");
        }
    }

}
