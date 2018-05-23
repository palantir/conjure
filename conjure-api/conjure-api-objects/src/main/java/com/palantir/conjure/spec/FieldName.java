package com.palantir.conjure.spec;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Objects;
import javax.annotation.Generated;

/** Should be in lowerCamelCase, but kebab-case and snake_case are also permitted. */
@Generated("com.palantir.conjure.gen.java.types.AliasGenerator")
public final class FieldName {
    private final String value;

    private FieldName(String value) {
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
                || (other instanceof FieldName && this.value.equals(((FieldName) other).value));
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    public static FieldName valueOf(String value) {
        return new FieldName(value);
    }

    @JsonCreator
    public static FieldName of(String value) {
        return new FieldName(value);
    }
}
