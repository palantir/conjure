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

@JsonDeserialize(builder = UnionDefinition.Builder.class)
@Generated("com.palantir.conjure.gen.java.types.BeanGenerator")
public final class UnionDefinition {
    private final TypeName typeName;

    private final List<FieldDefinition> union;

    private final Optional<Documentation> docs;

    private volatile int memoizedHashCode;

    private UnionDefinition(
            TypeName typeName, List<FieldDefinition> union, Optional<Documentation> docs) {
        validateFields(typeName, union, docs);
        this.typeName = typeName;
        this.union = Collections.unmodifiableList(union);
        this.docs = docs;
    }

    @JsonProperty("typeName")
    public TypeName getTypeName() {
        return this.typeName;
    }

    @JsonProperty("union")
    public List<FieldDefinition> getUnion() {
        return this.union;
    }

    @JsonProperty("docs")
    public Optional<Documentation> getDocs() {
        return this.docs;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || (other instanceof UnionDefinition && equalTo((UnionDefinition) other));
    }

    private boolean equalTo(UnionDefinition other) {
        return this.typeName.equals(other.typeName)
                && this.union.equals(other.union)
                && this.docs.equals(other.docs);
    }

    @Override
    public int hashCode() {
        if (memoizedHashCode == 0) {
            memoizedHashCode = Objects.hash(typeName, union, docs);
        }
        return memoizedHashCode;
    }

    @Override
    public String toString() {
        return new StringBuilder("UnionDefinition")
                .append("{")
                .append("typeName")
                .append(": ")
                .append(typeName)
                .append(", ")
                .append("union")
                .append(": ")
                .append(union)
                .append(", ")
                .append("docs")
                .append(": ")
                .append(docs)
                .append("}")
                .toString();
    }

    public static UnionDefinition of(
            TypeName typeName, List<FieldDefinition> union, Documentation docs) {
        return builder().typeName(typeName).union(union).docs(Optional.of(docs)).build();
    }

    private static void validateFields(
            TypeName typeName, List<FieldDefinition> union, Optional<Documentation> docs) {
        List<String> missingFields = null;
        missingFields = addFieldIfMissing(missingFields, typeName, "typeName");
        missingFields = addFieldIfMissing(missingFields, union, "union");
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

        private List<FieldDefinition> union = new ArrayList<>();

        private Optional<Documentation> docs = Optional.empty();

        private Builder() {}

        public Builder from(UnionDefinition other) {
            typeName(other.getTypeName());
            union(other.getUnion());
            docs(other.getDocs());
            return this;
        }

        @JsonSetter("typeName")
        public Builder typeName(TypeName typeName) {
            this.typeName = Objects.requireNonNull(typeName, "typeName cannot be null");
            return this;
        }

        @JsonSetter("union")
        public Builder union(Iterable<FieldDefinition> union) {
            this.union.clear();
            ConjureCollections.addAll(
                    this.union, Objects.requireNonNull(union, "union cannot be null"));
            return this;
        }

        public Builder addAllUnion(Iterable<FieldDefinition> union) {
            ConjureCollections.addAll(
                    this.union, Objects.requireNonNull(union, "union cannot be null"));
            return this;
        }

        public Builder union(FieldDefinition union) {
            this.union.add(union);
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

        public UnionDefinition build() {
            return new UnionDefinition(typeName, union, docs);
        }
    }
}
