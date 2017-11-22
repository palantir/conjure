package com.palantir.product;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Generated;

@Generated("com.palantir.conjure.gen.java.types.AliasGenerator")
public final class StringAliasExample implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String value;

    private StringAliasExample(String value) {
        Objects.requireNonNull(value, "value cannot be null");
        this.value = value;
    }

    @JsonValue
    public String get() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || (other instanceof StringAliasExample
                        && this.value.equals(((StringAliasExample) other).value));
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    public static StringAliasExample valueOf(String value) {
        return new StringAliasExample(value);
    }

    @JsonCreator
    public static StringAliasExample of(String value) {
        return new StringAliasExample(value);
    }
}
