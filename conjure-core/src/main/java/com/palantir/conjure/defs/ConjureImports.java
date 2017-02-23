/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs;

import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import java.util.Collection;
import java.util.Map;

public final class ConjureImports {
    private static final String NAMESPACE_PATTERN = "[a-z]{2,10}";

    private final Map<String, ObjectsDefinition> typesByNamespace;

    public ConjureImports(Map<String, ObjectsDefinition> typesByNamespace) {
        this.typesByNamespace = typesByNamespace;

        Collection<String> illegalNamespaces =
                Collections2.filter(typesByNamespace.keySet(), ns -> !ns.matches(NAMESPACE_PATTERN));
        Preconditions.checkArgument(illegalNamespaces.isEmpty(),
                "The following namespaces do not satisfy the namespace pattern %s: %s",
                NAMESPACE_PATTERN, illegalNamespaces);
    }
}
