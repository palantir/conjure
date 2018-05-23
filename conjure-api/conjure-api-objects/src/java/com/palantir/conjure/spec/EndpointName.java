package com.palantir.conjure.spec;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Objects;
import javax.annotation.Generated;

/** Should be in lowerCamelCase. */
@Generated("com.palantir.conjure.gen.java.types.AliasGenerator")
public final class EndpointName {
    private final String value;

    private EndpointName(String value) {
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
                || (other instanceof EndpointName
                        && this.value.equals(((EndpointName) other).value));
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    public static EndpointName valueOf(String value) {
        return new EndpointName(value);
    }

    @JsonCreator
    public static EndpointName of(String value) {
        return new EndpointName(value);
    }
}
