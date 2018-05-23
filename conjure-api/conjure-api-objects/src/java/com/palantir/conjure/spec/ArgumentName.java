package com.palantir.conjure.spec;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Objects;
import javax.annotation.Generated;

/**
 * Must be in lowerCamelCase. Numbers are permitted, but not at the beginning of a word. Allowed
 * argument names: "fooBar", "build2Request". Disallowed names: "FooBar", "2BuildRequest".
 */
@Generated("com.palantir.conjure.gen.java.types.AliasGenerator")
public final class ArgumentName {
    private final String value;

    private ArgumentName(String value) {
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
                || (other instanceof ArgumentName
                        && this.value.equals(((ArgumentName) other).value));
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    public static ArgumentName valueOf(String value) {
        return new ArgumentName(value);
    }

    @JsonCreator
    public static ArgumentName of(String value) {
        return new ArgumentName(value);
    }
}
