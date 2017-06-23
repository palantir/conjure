/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import java.util.Map;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@Value.Immutable
@JsonSerialize(as = ImmutablePackageJson.class)
@JsonDeserialize(as = ImmutablePackageJson.class)
public interface PackageJson {

    String author();

    Map<String, String> peerDependencies();

    String version();

    String description();

    String name();

    Map<String, String> dependencies();

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutablePackageJson.Builder {}

}
