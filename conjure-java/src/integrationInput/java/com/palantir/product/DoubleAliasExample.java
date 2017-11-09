package com.palantir.product;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import javax.annotation.Generated;

@Generated("com.palantir.conjure.gen.java.types.AliasGenerator")
public final class DoubleAliasExample {
    private final double value;

    private DoubleAliasExample(double value) {
        this.value = value;
    }

    @JsonValue
    public double get() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || (other instanceof DoubleAliasExample
                        && this.value == ((DoubleAliasExample) other).value);
    }

    @Override
    public int hashCode() {
        return Double.hashCode(value);
    }

    public static DoubleAliasExample valueOf(String value) {
        return new DoubleAliasExample(Double.parseDouble(value));
    }

    @JsonCreator
    public static DoubleAliasExample of(double value) {
        return new DoubleAliasExample(value);
    }
}
