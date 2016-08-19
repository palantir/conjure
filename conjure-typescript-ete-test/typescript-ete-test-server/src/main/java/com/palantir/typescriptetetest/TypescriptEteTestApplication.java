/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.typescriptetetest;

import com.google.common.collect.Sets;
import com.palantir.config.crypto.EncryptedConfigValueBundle;
import com.palantir.indexpage.IndexPageBundle;
import com.palantir.multipass.dropwizard.AuthBundle;
import com.palantir.remoting.http.server.DropwizardTracingFilters;
import com.palantir.typescriptetetest.resources.CalculatorResource;
import com.palantir.websecurity.WebSecurityBundle;
import io.dropwizard.Application;
import io.dropwizard.bundles.assets.ConfiguredAssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public final class TypescriptEteTestApplication extends Application<TypescriptEteTestConfiguration> {

    public static void main(String[] args) throws Exception {
        new TypescriptEteTestApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<TypescriptEteTestConfiguration> bootstrap) {
        bootstrap.addBundle(new AuthBundle<>());
        bootstrap.addBundle(new ConfiguredAssetsBundle("/assets/", "/", "index.html"));
        bootstrap.addBundle(new EncryptedConfigValueBundle());
        bootstrap.addBundle(new IndexPageBundle(Sets.newHashSet("/views/*")));
        bootstrap.addBundle(new WebSecurityBundle());
    }

    @Override
    public void run(TypescriptEteTestConfiguration configuration, Environment environment) {
        DropwizardTracingFilters.registerTracers(environment, configuration, "typescript-ete-test-server");
        environment.jersey().register(new CalculatorResource());
    }
}
