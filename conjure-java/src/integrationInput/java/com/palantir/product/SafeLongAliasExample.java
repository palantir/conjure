package com.palantir.product;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.palantir.conjure.lib.SafeLong;
import java.util.Objects;
import javax.annotation.Generated;

@Generated("com.palantir.conjure.gen.java.types.AliasGenerator")
public final class SafeLongAliasExample {
    private final SafeLong value;

    private SafeLongAliasExample(SafeLong value) {
        Objects.requireNonNull(value, "value cannot be null");
        this.value = value;
    }

    @JsonValue
    public SafeLong get() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || (other instanceof SafeLongAliasExample
                        && this.value.equals(((SafeLongAliasExample) other).value));
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    public static SafeLongAliasExample valueOf(String value) {
        return new SafeLongAliasExample(SafeLong.valueOf(value));
    }

    @JsonCreator
    public static SafeLongAliasExample of(SafeLong value) {
        return new SafeLongAliasExample(value);
    }
}
