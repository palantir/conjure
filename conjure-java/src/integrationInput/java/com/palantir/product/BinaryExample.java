package com.palantir.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Generated;

@JsonDeserialize(builder = BinaryExample.Builder.class)
@Generated("com.palantir.conjure.gen.java.types.BeanGenerator")
public final class BinaryExample {
    private final ByteBuffer binary;

    private BinaryExample(ByteBuffer binary) {
        validateFields(binary);
        this.binary = binary;
    }

    @JsonProperty("binary")
    public ByteBuffer getBinary() {
        return this.binary.asReadOnlyBuffer();
    }

    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof BinaryExample && equalTo((BinaryExample) other));
    }

    private boolean equalTo(BinaryExample other) {
        return this.binary.equals(other.binary);
    }

    @Override
    public int hashCode() {
        return Objects.hash(binary);
    }

    @Override
    public String toString() {
        return new StringBuilder("BinaryExample")
                .append("{")
                .append("binary")
                .append(": ")
                .append(binary)
                .append("}")
                .toString();
    }

    public static BinaryExample of(ByteBuffer binary) {
        return builder().binary(binary).build();
    }

    private static void validateFields(ByteBuffer binary) {
        List<String> missingFields = null;
        missingFields = addFieldIfMissing(missingFields, binary, "binary");
        if (missingFields != null) {
            throw new IllegalArgumentException(
                    "Some required fields have not been set: " + missingFields);
        }
    }

    private static List<String> addFieldIfMissing(
            List<String> prev, Object fieldValue, String fieldName) {
        List<String> missingFields = prev;
        if (fieldValue == null) {
            if (missingFields == null) {
                missingFields = new ArrayList<>(1);
            }
            missingFields.add(fieldName);
        }
        return missingFields;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Generated("com.palantir.conjure.gen.java.types.BeanBuilderGenerator")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
        private ByteBuffer binary;

        private Builder() {}

        public Builder from(BinaryExample other) {
            binary(other.getBinary());
            return this;
        }

        @JsonSetter("binary")
        public Builder binary(ByteBuffer binary) {
            Objects.requireNonNull(binary, "binary cannot be null");
            this.binary = ByteBuffer.allocate(binary.remaining()).put(binary.duplicate());
            this.binary.rewind();
            return this;
        }

        public BinaryExample build() {
            return new BinaryExample(binary);
        }
    }
}
