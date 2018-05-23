package com.palantir.conjure.spec;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.conjure.lib.internal.ConjureCollections;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Generated;

@JsonDeserialize(builder = ObjectDefinition.Builder.class)
@Generated("com.palantir.conjure.gen.java.types.BeanGenerator")
public final class ObjectDefinition {
    private final TypeName typeName;

    private final List<FieldDefinition> fields;

    private final Optional<Documentation> docs;

    private volatile int memoizedHashCode;

    private ObjectDefinition(
            TypeName typeName, List<FieldDefinition> fields, Optional<Documentation> docs) {
        validateFields(typeName, fields, docs);
        this.typeName = typeName;
        this.fields = Collections.unmodifiableList(fields);
        this.docs = docs;
    }

    @JsonProperty("typeName")
    public TypeName getTypeName() {
        return this.typeName;
    }

    @JsonProperty("fields")
    public List<FieldDefinition> getFields() {
        return this.fields;
    }

    @JsonProperty("docs")
    public Optional<Documentation> getDocs() {
        return this.docs;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || (other instanceof ObjectDefinition && equalTo((ObjectDefinition) other));
    }

    private boolean equalTo(ObjectDefinition other) {
        return this.typeName.equals(other.typeName)
                && this.fields.equals(other.fields)
                && this.docs.equals(other.docs);
    }

    @Override
    public int hashCode() {
        if (memoizedHashCode == 0) {
            memoizedHashCode = Objects.hash(typeName, fields, docs);
        }
        return memoizedHashCode;
    }

    @Override
    public String toString() {
        return new StringBuilder("ObjectDefinition")
                .append("{")
                .append("typeName")
                .append(": ")
                .append(typeName)
                .append(", ")
                .append("fields")
                .append(": ")
                .append(fields)
                .append(", ")
                .append("docs")
                .append(": ")
                .append(docs)
                .append("}")
                .toString();
    }

    public static ObjectDefinition of(
            TypeName typeName, List<FieldDefinition> fields, Documentation docs) {
        return builder().typeName(typeName).fields(fields).docs(Optional.of(docs)).build();
    }

    private static void validateFields(
            TypeName typeName, List<FieldDefinition> fields, Optional<Documentation> docs) {
        List<String> missingFields = null;
        missingFields = addFieldIfMissing(missingFields, typeName, "typeName");
        missingFields = addFieldIfMissing(missingFields, fields, "fields");
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
        private TypeName typeName;

        private List<FieldDefinition> fields = new ArrayList<>();

        private Optional<Documentation> docs = Optional.empty();

        private Builder() {}

        public Builder from(ObjectDefinition other) {
            typeName(other.getTypeName());
            fields(other.getFields());
            docs(other.getDocs());
            return this;
        }

        @JsonSetter("typeName")
        public Builder typeName(TypeName typeName) {
            this.typeName = Objects.requireNonNull(typeName, "typeName cannot be null");
            return this;
        }

        @JsonSetter("fields")
        public Builder fields(Iterable<FieldDefinition> fields) {
            this.fields.clear();
            ConjureCollections.addAll(
                    this.fields, Objects.requireNonNull(fields, "fields cannot be null"));
            return this;
        }

        public Builder addAllFields(Iterable<FieldDefinition> fields) {
            ConjureCollections.addAll(
                    this.fields, Objects.requireNonNull(fields, "fields cannot be null"));
            return this;
        }

        public Builder fields(FieldDefinition fields) {
            this.fields.add(fields);
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

        public ObjectDefinition build() {
            return new ObjectDefinition(typeName, fields, docs);
        }
    }
}
