package com.palantir.conjure.spec;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import javax.annotation.Generated;

@JsonSerialize
@Generated("com.palantir.conjure.gen.java.types.BeanGenerator")
public final class PathParameterType {
    private static final PathParameterType INSTANCE = new PathParameterType();

    private PathParameterType() {}

    @Override
    public String toString() {
        return new StringBuilder("PathParameterType").append("{").append("}").toString();
    }

    @JsonCreator
    public static PathParameterType of() {
        return INSTANCE;
    }
}
