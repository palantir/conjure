/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.collect.Collections2;
import com.palantir.conjure.defs.types.BaseObjectTypeDefinition;
import com.palantir.conjure.defs.types.ReferenceType;
import java.util.Collection;
import java.util.Map;

public final class ConjureImports {
    private static final String NAMESPACE_PATTERN = "[a-zA-Z]+";

    private final Map<String, ObjectsDefinition> typesByNamespace;

    public ConjureImports(Map<String, ObjectsDefinition> typesByNamespace) {
        this.typesByNamespace = typesByNamespace;

        Collection<String> illegalNamespaces =
                Collections2.filter(typesByNamespace.keySet(), ns -> !ns.matches(NAMESPACE_PATTERN));
        Preconditions.checkArgument(illegalNamespaces.isEmpty(),
                "The following namespaces do not satisfy the namespace pattern %s: %s",
                NAMESPACE_PATTERN, illegalNamespaces);
    }

    public String getPackage(ReferenceType type) {
        Verify.verifyNotNull(type.namespace().isPresent(),
                "Must not call ConjureImports methods for ReferenceType without namespace: %s", type);
        ObjectsDefinition imports = getImportsForRefNameSpace(type);
        BaseObjectTypeDefinition typeDef = Verify.verifyNotNull(imports.objects().get(type.type()),
                "Imported type not found: %s", type);
        return typeDef.packageName().orElse(getImportsForRefNameSpace(type).defaultPackage());
    }

    private ObjectsDefinition getImportsForRefNameSpace(ReferenceType type) {
        return Verify.verifyNotNull(typesByNamespace.get(type.namespace().get()),
                "No imported namespace found for reference type: %s", type);
    }
}
