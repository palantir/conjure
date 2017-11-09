package com.palantir.product;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.palantir.tokens.auth.BearerToken;
import java.util.Objects;
import javax.annotation.Generated;

@Generated("com.palantir.conjure.gen.java.types.AliasGenerator")
public final class BearerTokenAliasExample {
    private final BearerToken value;

    private BearerTokenAliasExample(BearerToken value) {
        Objects.requireNonNull(value, "value cannot be null");
        this.value = value;
    }

    @JsonValue
    public BearerToken get() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || (other instanceof BearerTokenAliasExample
                        && this.value.equals(((BearerTokenAliasExample) other).value));
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    public static BearerTokenAliasExample valueOf(String value) {
        return new BearerTokenAliasExample(BearerToken.valueOf(value));
    }

    @JsonCreator
    public static BearerTokenAliasExample of(BearerToken value) {
        return new BearerTokenAliasExample(value);
    }
}
