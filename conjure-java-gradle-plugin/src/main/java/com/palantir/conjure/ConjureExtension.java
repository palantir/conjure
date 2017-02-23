/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure;

import com.palantir.conjure.gen.java.types.TypeMapper;
import java.util.Arrays;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.file.collections.SimpleFileCollection;

public class ConjureExtension {
    private TypeMapper.OptionalTypeStrategy optionalType = TypeMapper.OptionalTypeStrategy.GUAVA;
    private FileCollection conjureImports = new SimpleFileCollection();

    public final TypeMapper.OptionalTypeStrategy getOptionalType() {
        return optionalType;
    }

    public final void setOptionalType(String optionalType) {
        try {
            this.optionalType = TypeMapper.OptionalTypeStrategy.valueOf(optionalType);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unknown Optional type: " + optionalType + ". Known types: "
                    + Arrays.toString(TypeMapper.OptionalTypeStrategy.values()));
        }
    }

    public final FileCollection getConjureImports() {
        return conjureImports;
    }

    public final void setConjureImports(FileCollection files) {
        conjureImports = files;
    }
}
