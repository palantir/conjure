/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

public enum PrimitiveType implements ReferenceType {
    String("String"),
    Integer("Integer"),
    Double("Double");

    private final String type;

    PrimitiveType(String type) {
        this.type = type;
    }

    @Override
    public String type() {
        return type;
    }
}
