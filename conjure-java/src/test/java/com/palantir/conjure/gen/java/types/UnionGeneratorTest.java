/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

import com.palantir.conjure.defs.Conjure;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.gen.java.ExperimentalFeatures;
import com.palantir.conjure.gen.java.Settings;
import java.io.File;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class UnionGeneratorTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testUnionTypeIsAnExperimentalFeature() {
        ConjureDefinition def = Conjure.parse(new File("src/test/resources/example-types.yml"));
        BeanGenerator generator = new BeanGenerator(Settings.standard());

        try {
            generator.emit(def, folder.getRoot());
            failBecauseExceptionWasNotThrown(ExperimentalFeatureDisabledException.class);
        } catch (ExperimentalFeatureDisabledException e) {
            assertThat(e.getFeature()).isEqualTo(ExperimentalFeatures.UnionTypes);
        }
    }
}
