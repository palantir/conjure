package com.palantir.conjure.spec;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Generated;

@JsonDeserialize(builder = EnumValueDefinition.Builder.class)
@Generated("com.palantir.conjure.gen.java.types.BeanGenerator")
public final class EnumValueDefinition {
    private final String value;

    private final Optional<Documentation> docs;

    private volatile int memoizedHashCode;

    private EnumValueDefinition(String value, Optional<Documentation> docs) {
        validateFields(value, docs);
        this.value = value;
        this.docs = docs;
    }

    @JsonProperty("value")
    public String getValue() {
        return this.value;
    }

    @JsonProperty("docs")
    public Optional<Documentation> getDocs() {
        return this.docs;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || (other instanceof EnumValueDefinition && equalTo((EnumValueDefinition) other));
    }

    private boolean equalTo(EnumValueDefinition other) {
        return this.value.equals(other.value) && this.docs.equals(other.docs);
    }

    @Override
    public int hashCode() {
        if (memoizedHashCode == 0) {
            memoizedHashCode = Objects.hash(value, docs);
        }
        return memoizedHashCode;
    }

    @Override
    public String toString() {
        return new StringBuilder("EnumValueDefinition")
                .append("{")
                .append("value")
                .append(": ")
                .append(value)
                .append(", ")
                .append("docs")
                .append(": ")
                .append(docs)
                .append("}")
                .toString();
    }

    public static EnumValueDefinition of(String value, Documentation docs) {
        return builder().value(value).docs(Optional.of(docs)).build();
    }

    private static void validateFields(String value, Optional<Documentation> docs) {
        List<String> missingFields = null;
        missingFields = addFieldIfMissing(missingFields, value, "value");
        missingFields = addFieldIfMissing(missingFields, docs, "docs");
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
                missingFields = new ArrayList<>(2);
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
        private String value;

        private Optional<Documentation> docs = Optional.empty();

        private Builder() {}

        public Builder from(EnumValueDefinition other) {
            value(other.getValue());
            docs(other.getDocs());
            return this;
        }

        @JsonSetter("value")
        public Builder value(String value) {
            this.value = Objects.requireNonNull(value, "value cannot be null");
            return this;
        }

        @JsonSetter("docs")
        public Builder docs(Optional<Documentation> docs) {
            this.docs = Objects.requireNonNull(docs, "docs cannot be null");
            return this;
        }

        public Builder docs(Documentation docs) {
            this.docs = Optional.of(Objects.requireNonNull(docs, "docs cannot be null"));
            return this;
        }

        public EnumValueDefinition build() {
            return new EnumValueDefinition(value, docs);
        }
    }
}
