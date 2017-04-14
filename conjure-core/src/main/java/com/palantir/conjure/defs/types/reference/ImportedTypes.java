/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.reference;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.base.Verify;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.types.BaseObjectTypeDefinition;
import com.palantir.conjure.defs.types.ObjectsDefinition;
import com.palantir.conjure.defs.types.names.ConjurePackage;
import com.palantir.conjure.defs.types.names.ConjurePackages;
import java.io.File;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface ImportedTypes {

    /**
     * The file from which types are to be imported. The path is relative to the directory in which the declaring
     * top-level ConjureDefinition file lives.
     */
    String file();

    /**
     * The types defined in the imported {@link #file}. Note that this may be empty and is typically only populated in
     * {@link com.palantir.conjure.defs.Conjure#parse(File)}.
     * TODO(rfink): Can we make this better, maybe with custom parsing?
     */
    ObjectsDefinition importedTypes();

    @Value.Lazy
    default ConjurePackage getPackageForImportedType(ForeignReferenceType type) {
        BaseObjectTypeDefinition typeDef = Verify.verifyNotNull(importedTypes().objects().get(type.type()),
                "Imported type not found: %s", type);
        return ConjurePackages.getPackage(
                typeDef.conjurePackage(),
                importedTypes().defaultConjurePackage(), type.type());
    }

    @JsonCreator
    static ImportedTypes fromFile(String file) {
        return ImmutableImportedTypes.builder()
                .file(file)
                .importedTypes(ObjectsDefinition.builder().build())
                .build();
    }

    static ImportedTypes withResolvedImports(String file, ObjectsDefinition importedTypes) {
        return ImmutableImportedTypes.builder().file(file).importedTypes(importedTypes).build();
    }
}
