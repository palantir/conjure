package com.palantir.product;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.Serializable;
import javax.annotation.Generated;

@JsonSerialize
@Generated("com.palantir.conjure.gen.java.types.BeanGenerator")
public final class EmptyObjectExample implements Serializable {
    private static final long serialVersionUID = 1L;

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
