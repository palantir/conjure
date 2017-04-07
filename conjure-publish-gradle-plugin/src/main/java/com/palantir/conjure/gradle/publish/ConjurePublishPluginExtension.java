/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gradle.publish;

public class ConjurePublishPluginExtension {
    private String scopeName = "elements";

    public final String getScopeName() {
        return scopeName;
    }

    public final void setScopeName(String scopeName) {
        this.scopeName = scopeName;
    }
}
