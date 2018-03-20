package com.palantir.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Generated;

@JsonDeserialize(builder = IntegerExample.Builder.class)
@Generated("com.palantir.conjure.gen.java.types.BeanGenerator")
public final class IntegerExample implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int integer;

    private transient volatile int memoizedHashCode;

    private IntegerExample(int integer) {
        this.integer = integer;
    }

    @JsonProperty("integer")
    public int getInteger() {
        return this.integer;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || (other instanceof IntegerExample && equalTo((IntegerExample) other));
    }

    private boolean equalTo(IntegerExample other) {
        return this.integer == other.integer;
    }

    @Override
    public int hashCode() {
        if (memoizedHashCode == 0) {
            memoizedHashCode = Objects.hash(integer);
        }
        return memoizedHashCode;
    }

    @Override
    public String toString() {
        return new StringBuilder("IntegerExample")
                .append("{")
                .append("integer")
                .append(": ")
                .append(integer)
                .append("}")
                .toString();
    }

    public static IntegerExample of(int integer) {
        return builder().integer(integer).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    @Generated("com.palantir.conjure.gen.java.types.BeanBuilderGenerator")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
        private int integer;

        private Builder() {}

        public Builder from(IntegerExample other) {
            integer(other.getInteger());
            return this;
        }

        @JsonSetter("integer")
        public Builder integer(int integer) {
            this.integer = integer;
            return this;
        }

        public IntegerExample build() {
            return new IntegerExample(integer);
        }
    }
}
