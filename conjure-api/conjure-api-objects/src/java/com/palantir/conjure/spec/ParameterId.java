package com.palantir.conjure.spec;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Objects;
import javax.annotation.Generated;

/**
 * For header parameters, the parameter id must be in Upper-Kebab-Case. For query parameters, the
 * parameter id must be in lowerCamelCase. Numbers are permitted, but not at the beginning of a
 * word.
 */
@Generated("com.palantir.conjure.gen.java.types.AliasGenerator")
public final class ParameterId {
    private final String value;

    private ParameterId(String value) {
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
                || (other instanceof ParameterId && this.value.equals(((ParameterId) other).value));
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    public static ParameterId valueOf(String value) {
        return new ParameterId(value);
    }

    @JsonCreator
    public static ParameterId of(String value) {
        return new ParameterId(value);
    }
}
