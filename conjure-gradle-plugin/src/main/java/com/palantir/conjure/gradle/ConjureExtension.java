/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gradle;

import groovy.lang.Closure;
import java.io.File;
import java.util.Optional;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.file.collections.SimpleFileCollection;
import org.gradle.util.ConfigureUtil;

public class ConjureExtension {

    private Optional<JerseyServerConfig> jerseyServer = Optional.empty();
    private Optional<JerseyClientConfig> jerseyClient = Optional.empty();
    private Optional<RetrofitClientConfig> retrofitClient = Optional.empty();
    private Optional<TypeScriptClientConfig> typeScriptClient = Optional.empty();

    private FileCollection conjureImports = new SimpleFileCollection();

    public final void setJerseyServer(Closure<?> closure) {
        jerseyServer = Optional.of(ConfigureUtil.configure(closure, new JerseyServerConfig()));
    }

    public final Optional<JerseyServerConfig> getJerseyServer() {
        return jerseyServer;
    }

    public final void setJerseyClient(Closure<?> closure) {
        jerseyClient = Optional.of(ConfigureUtil.configure(closure, new JerseyClientConfig()));
    }

    public final Optional<JerseyClientConfig> getJerseyClient() {
        return jerseyClient;
    }

    public final void setRetrofitClient(Closure<?> closure) {
        retrofitClient = Optional.of(ConfigureUtil.configure(closure, new RetrofitClientConfig()));
    }

    public final Optional<RetrofitClientConfig> getRetrofitClient() {
        return retrofitClient;
    }

    public final void setTypeScriptClient(Closure<?> closure) {
        typeScriptClient = Optional.of(ConfigureUtil.configure(closure, new TypeScriptClientConfig()));
    }

    public final Optional<TypeScriptClientConfig> getTypeScriptClient() {
        return typeScriptClient;
    }

    public final void setConjureImports(FileCollection files) {
        conjureImports = files;
    }

    public final void conjureImports(FileCollection files) {
        conjureImports = files;
    }

    public final FileCollection getConjureImports() {
        return conjureImports;
    }

    public abstract static class BaseConfig {
        private File output;

        public final void output(String loc) {
            this.output = new File(loc);
        }

        public final void output(File loc) {
            this.output = loc;
        }

        public final File getOutput() {
            return output;
        }
    }

    public static class JerseyServerConfig extends BaseConfig {}

    public static class JerseyClientConfig extends BaseConfig {}

    public static class RetrofitClientConfig extends BaseConfig {}

    public static class TypeScriptClientConfig extends BaseConfig {}
}
