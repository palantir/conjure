/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gradle.publish;

import com.google.common.base.Preconditions;
import org.gradle.api.Project;

public class ConjurePublishPluginExtension {
    private String scopeName;
    private String packageName;

    public ConjurePublishPluginExtension(Project project) {
        this.scopeName = "elements";
        this.packageName = project.getName().replace("-typescript", "");
    }

    public final String getScopeName() {
        return scopeName;
    }

    public final String getPackageName() {
        return packageName;
    }

    public final void setScopeName(String scopeName) {
        Preconditions.checkNotNull(scopeName, "invalid scope name");

        this.scopeName = scopeName;
    }

    public final void setPackageName(String packageName) {
        Preconditions.checkNotNull(packageName, "invalid package name");

        this.packageName = packageName;
    }
}
