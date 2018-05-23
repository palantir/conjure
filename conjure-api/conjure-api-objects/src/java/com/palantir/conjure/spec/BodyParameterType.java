package com.palantir.conjure.spec;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import javax.annotation.Generated;

@JsonSerialize
@Generated("com.palantir.conjure.gen.java.types.BeanGenerator")
public final class BodyParameterType {
    private static final BodyParameterType INSTANCE = new BodyParameterType();

    private BodyParameterType() {}

    @Override
    public String toString() {
        return new StringBuilder("BodyParameterType").append("{").append("}").toString();
    }

    @JsonCreator
    public static BodyParameterType of() {
        return INSTANCE;
    }
}
