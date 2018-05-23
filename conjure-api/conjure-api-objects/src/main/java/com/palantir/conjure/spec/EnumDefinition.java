package com.palantir.conjure.spec;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.conjure.java.lib.internal.ConjureCollections;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Generated;

@JsonDeserialize(builder = EnumDefinition.Builder.class)
@Generated("com.palantir.conjure.gen.java.types.BeanGenerator")
public final class EnumDefinition {
    private final TypeName typeName;

    private final List<EnumValueDefinition> values;

    private final Optional<Documentation> docs;

    private volatile int memoizedHashCode;

    private EnumDefinition(
            TypeName typeName, List<EnumValueDefinition> values, Optional<Documentation> docs) {
        validateFields(typeName, values, docs);
        this.typeName = typeName;
        this.values = Collections.unmodifiableList(values);
        this.docs = docs;
    }

    @JsonProperty("typeName")
    public TypeName getTypeName() {
        return this.typeName;
    }

    @JsonProperty("values")
    public List<EnumValueDefinition> getValues() {
        return this.values;
    }

    @JsonProperty("docs")
    public Optional<Documentation> getDocs() {
        return this.docs;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || (other instanceof EnumDefinition && equalTo((EnumDefinition) other));
    }

    private boolean equalTo(EnumDefinition other) {
        return this.typeName.equals(other.typeName)
                && this.values.equals(other.values)
                && this.docs.equals(other.docs);
    }

    @Override
    public int hashCode() {
        if (memoizedHashCode == 0) {
            memoizedHashCode = Objects.hash(typeName, values, docs);
        }
        return memoizedHashCode;
    }

    @Override
    public String toString() {
        return new StringBuilder("EnumDefinition")
                .append("{")
                .append("typeName")
                .append(": ")
                .append(typeName)
                .append(", ")
                .append("values")
                .append(": ")
                .append(values)
                .append(", ")
                .append("docs")
                .append(": ")
                .append(docs)
                .append("}")
                .toString();
    }

    public static EnumDefinition of(
            TypeName typeName, List<EnumValueDefinition> values, Documentation docs) {
        return builder().typeName(typeName).values(values).docs(Optional.of(docs)).build();
    }

    private static void validateFields(
            TypeName typeName, List<EnumValueDefinition> values, Optional<Documentation> docs) {
        List<String> missingFields = null;
        missingFields = addFieldIfMissing(missingFields, typeName, "typeName");
        missingFields = addFieldIfMissing(missingFields, values, "values");
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

        private List<EnumValueDefinition> values = new ArrayList<>();

        private Optional<Documentation> docs = Optional.empty();

        private Builder() {}

        public Builder from(EnumDefinition other) {
            typeName(other.getTypeName());
            values(other.getValues());
            docs(other.getDocs());
            return this;
        }

        @JsonSetter("typeName")
        public Builder typeName(TypeName typeName) {
            this.typeName = Objects.requireNonNull(typeName, "typeName cannot be null");
            return this;
        }

        @JsonSetter("values")
        public Builder values(Iterable<EnumValueDefinition> values) {
            this.values.clear();
            ConjureCollections.addAll(
                    this.values, Objects.requireNonNull(values, "values cannot be null"));
            return this;
        }

        public Builder addAllValues(Iterable<EnumValueDefinition> values) {
            ConjureCollections.addAll(
                    this.values, Objects.requireNonNull(values, "values cannot be null"));
            return this;
        }

        public Builder values(EnumValueDefinition values) {
            this.values.add(values);
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

        public EnumDefinition build() {
            return new EnumDefinition(typeName, values, docs);
        }
    }
}
