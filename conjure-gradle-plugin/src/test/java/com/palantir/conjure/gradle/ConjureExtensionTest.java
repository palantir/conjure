/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gradle;

import static org.assertj.core.api.Assertions.assertThat;

import com.palantir.conjure.gen.java.ExperimentalFeatures;
import org.junit.Before;
import org.junit.Test;

public class ConjureExtensionTest {
    private final ConjureExtension extension = new ConjureExtension();

    @Before
    public void before() {
        extension.experimentalFeature("TypeScriptErrorTypes");
        extension.experimentalFeature("ErrorTypes");
    }

    @Test
    public void getJavaExperimentalFeatures_shouldnt_explode_on_unknown_features() throws Exception {
        assertThat(extension.getJavaExperimentalFeatures())
                .containsExactly(ExperimentalFeatures.ErrorTypes);
    }

    @Test
    public void getPythonExperimentalFeatures_shouldnt_explode_on_unknown_features() throws Exception {
        assertThat(extension.getPythonExperimentalFeatures()).isEmpty();
    }

    @Test
    public void getTypescriptExperimentalFeatures_shouldnt_explode_on_unknown_features() throws Exception {
        assertThat(extension.getTypescriptExperimentalFeatures())
                .containsExactly(com.palantir.conjure.gen.typescript.ExperimentalFeatures.TypeScriptErrorTypes);
    }
}
