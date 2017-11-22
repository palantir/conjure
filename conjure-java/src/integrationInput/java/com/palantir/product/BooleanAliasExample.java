package com.palantir.product;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.Serializable;
import javax.annotation.Generated;

@Generated("com.palantir.conjure.gen.java.types.AliasGenerator")
public final class BooleanAliasExample implements Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean value;

    private BooleanAliasExample(boolean value) {
        this.value = value;
    }

    @JsonValue
    public boolean get() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || (other instanceof BooleanAliasExample
                        && this.value == ((BooleanAliasExample) other).value);
    }

    @Override
    public int hashCode() {
        return Boolean.hashCode(value);
    }

    public static BooleanAliasExample valueOf(String value) {
        return new BooleanAliasExample(Boolean.parseBoolean(value));
    }

    @JsonCreator
    public static BooleanAliasExample of(boolean value) {
        return new BooleanAliasExample(value);
    }
}
