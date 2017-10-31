/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;

public final class ConjureAnnotations {

    public static AnnotationSpec getConjureGeneratedAnnotation(Class<?> clazz) {
        return AnnotationSpec.builder(ClassName.get("javax.annotation", "Generated"))
                .addMember("value", "$S", clazz.getCanonicalName())
                .build();
    }

    private ConjureAnnotations() {}
}
