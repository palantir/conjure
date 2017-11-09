package com.palantir.product;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import javax.annotation.Generated;

@JsonSerialize
@Generated("com.palantir.conjure.gen.java.types.BeanGenerator")
public final class EmptyObjectExample {
    private static final EmptyObjectExample INSTANCE = new EmptyObjectExample();

    private EmptyObjectExample() {}

    @Override
    public String toString() {
        return new StringBuilder("EmptyObjectExample").append("{").append("}").toString();
    }

    @JsonCreator
    public static EmptyObjectExample of() {
        return INSTANCE;
    }
}
