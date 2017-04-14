/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.reference;

import com.google.common.base.Verify;
import com.palantir.conjure.defs.types.BaseObjectTypeDefinition;
import com.palantir.conjure.defs.types.ObjectsDefinition;
import com.palantir.conjure.defs.types.names.ConjurePackage;
import com.palantir.conjure.defs.types.names.ConjurePackages;
import com.palantir.conjure.defs.types.names.Namespace;
import java.util.Map;

public final class ConjureImports {
    private final Map<Namespace, ObjectsDefinition> typesByNamespace;

    public ConjureImports(Map<Namespace, ObjectsDefinition> typesByNamespace) {
        this.typesByNamespace = typesByNamespace;
    }

    public ConjurePackage getPackage(ReferenceType type) {
        Verify.verifyNotNull(type.namespace().isPresent(),
                "Must not call ConjureImports methods for ReferenceType without namespace: %s", type);
        ObjectsDefinition imports = getImportsForRefNameSpace(type);
        BaseObjectTypeDefinition typeDef = Verify.verifyNotNull(imports.objects().get(type.type()),
                "Imported type not found: %s", type);
        return ConjurePackages.getPackage(typeDef.conjurePackage(),
                getImportsForRefNameSpace(type).defaultConjurePackage(), type.type());
    }

    private ObjectsDefinition getImportsForRefNameSpace(ReferenceType type) {
        return Verify.verifyNotNull(typesByNamespace.get(type.namespace().get()),
                "No imported namespace found for reference type: %s", type);
    }
}
