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

@JsonDeserialize(builder = FieldDefinition.Builder.class)
@Generated("com.palantir.conjure.gen.java.types.BeanGenerator")
public final class FieldDefinition {
    private final FieldName fieldName;

    private final Type type;

    private final Optional<Documentation> docs;

    private volatile int memoizedHashCode;

    private FieldDefinition(FieldName fieldName, Type type, Optional<Documentation> docs) {
        validateFields(fieldName, type, docs);
        this.fieldName = fieldName;
        this.type = type;
        this.docs = docs;
    }

    @JsonProperty("fieldName")
    public FieldName getFieldName() {
        return this.fieldName;
    }

    @JsonProperty("type")
    public Type getType() {
        return this.type;
    }

    @JsonProperty("docs")
    public Optional<Documentation> getDocs() {
        return this.docs;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || (other instanceof FieldDefinition && equalTo((FieldDefinition) other));
    }

    private boolean equalTo(FieldDefinition other) {
        return this.fieldName.equals(other.fieldName)
                && this.type.equals(other.type)
                && this.docs.equals(other.docs);
    }

    @Override
    public int hashCode() {
        if (memoizedHashCode == 0) {
            memoizedHashCode = Objects.hash(fieldName, type, docs);
        }
        return memoizedHashCode;
    }

    @Override
    public String toString() {
        return new StringBuilder("FieldDefinition")
                .append("{")
                .append("fieldName")
                .append(": ")
                .append(fieldName)
                .append(", ")
                .append("type")
                .append(": ")
                .append(type)
                .append(", ")
                .append("docs")
                .append(": ")
                .append(docs)
                .append("}")
                .toString();
    }

    public static FieldDefinition of(FieldName fieldName, Type type, Documentation docs) {
        return builder().fieldName(fieldName).type(type).docs(Optional.of(docs)).build();
    }

    private static void validateFields(
            FieldName fieldName, Type type, Optional<Documentation> docs) {
        List<String> missingFields = null;
        missingFields = addFieldIfMissing(missingFields, fieldName, "fieldName");
        missingFields = addFieldIfMissing(missingFields, type, "type");
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
                missingFields = new ArrayList<>(3);
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
        private FieldName fieldName;

        private Type type;

        private Optional<Documentation> docs = Optional.empty();

        private Builder() {}

        public Builder from(FieldDefinition other) {
            fieldName(other.getFieldName());
            type(other.getType());
            docs(other.getDocs());
            return this;
        }

        @JsonSetter("fieldName")
        public Builder fieldName(FieldName fieldName) {
            this.fieldName = Objects.requireNonNull(fieldName, "fieldName cannot be null");
            return this;
        }

        @JsonSetter("type")
        public Builder type(Type type) {
            this.type = Objects.requireNonNull(type, "type cannot be null");
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

        public FieldDefinition build() {
            return new FieldDefinition(fieldName, type, docs);
        }
    }
}
