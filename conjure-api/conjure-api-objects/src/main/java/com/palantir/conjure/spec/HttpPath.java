package com.palantir.conjure.spec;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Objects;
import javax.annotation.Generated;

@Generated("com.palantir.conjure.gen.java.types.AliasGenerator")
public final class HttpPath {
    private final String value;

    private HttpPath(String value) {
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
                || (other instanceof HttpPath && this.value.equals(((HttpPath) other).value));
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    public static HttpPath valueOf(String value) {
        return new HttpPath(value);
    }

    @JsonCreator
    public static HttpPath of(String value) {
        return new HttpPath(value);
    }
}
