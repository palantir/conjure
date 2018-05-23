package com.palantir.conjure.spec;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import javax.annotation.Generated;

@JsonSerialize
@Generated("com.palantir.conjure.gen.java.types.BeanGenerator")
public final class HeaderAuthType {
    private static final HeaderAuthType INSTANCE = new HeaderAuthType();

    private HeaderAuthType() {}

    @Override
    public String toString() {
        return new StringBuilder("HeaderAuthType").append("{").append("}").toString();
    }

    @JsonCreator
    public static HeaderAuthType of() {
        return INSTANCE;
    }
}
