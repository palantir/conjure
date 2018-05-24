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

@JsonDeserialize(builder = AliasDefinition.Builder.class)
@Generated("com.palantir.conjure.gen.java.types.BeanGenerator")
public final class AliasDefinition {
    private final TypeName typeName;

    private final Type alias;

    private final Optional<Documentation> docs;

    private volatile int memoizedHashCode;

    private AliasDefinition(TypeName typeName, Type alias, Optional<Documentation> docs) {
        validateFields(typeName, alias, docs);
        this.typeName = typeName;
        this.alias = alias;
        this.docs = docs;
    }

    @JsonProperty("typeName")
    public TypeName getTypeName() {
        return this.typeName;
    }

    @JsonProperty("alias")
    public Type getAlias() {
        return this.alias;
    }

    @JsonProperty("docs")
    public Optional<Documentation> getDocs() {
        return this.docs;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || (other instanceof AliasDefinition && equalTo((AliasDefinition) other));
    }

    private boolean equalTo(AliasDefinition other) {
        return this.typeName.equals(other.typeName)
                && this.alias.equals(other.alias)
                && this.docs.equals(other.docs);
    }

    @Override
    public int hashCode() {
        if (memoizedHashCode == 0) {
            memoizedHashCode = Objects.hash(typeName, alias, docs);
        }
        return memoizedHashCode;
    }

    @Override
    public String toString() {
        return new StringBuilder("AliasDefinition")
                .append("{")
                .append("typeName")
                .append(": ")
                .append(typeName)
                .append(", ")
                .append("alias")
                .append(": ")
                .append(alias)
                .append(", ")
                .append("docs")
                .append(": ")
                .append(docs)
                .append("}")
                .toString();
    }

    public static AliasDefinition of(TypeName typeName, Type alias, Documentation docs) {
        return builder().typeName(typeName).alias(alias).docs(Optional.of(docs)).build();
    }

    private static void validateFields(
            TypeName typeName, Type alias, Optional<Documentation> docs) {
        List<String> missingFields = null;
        missingFields = addFieldIfMissing(missingFields, typeName, "typeName");
        missingFields = addFieldIfMissing(missingFields, alias, "alias");
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

        private Type alias;

        private Optional<Documentation> docs = Optional.empty();

        private Builder() {}

        public Builder from(AliasDefinition other) {
            typeName(other.getTypeName());
            alias(other.getAlias());
            docs(other.getDocs());
            return this;
        }

        @JsonSetter("typeName")
        public Builder typeName(TypeName typeName) {
            this.typeName = Objects.requireNonNull(typeName, "typeName cannot be null");
            return this;
        }

        @JsonSetter("alias")
        public Builder alias(Type alias) {
            this.alias = Objects.requireNonNull(alias, "alias cannot be null");
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

        public AliasDefinition build() {
            return new AliasDefinition(typeName, alias, docs);
        }
    }
}
