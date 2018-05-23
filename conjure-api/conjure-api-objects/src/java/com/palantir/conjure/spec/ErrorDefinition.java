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

@JsonDeserialize(builder = ErrorDefinition.Builder.class)
@Generated("com.palantir.conjure.gen.java.types.BeanGenerator")
public final class ErrorDefinition {
    private final TypeName errorName;

    private final Optional<Documentation> docs;

    private final ErrorNamespace namespace;

    private final ErrorCode code;

    private final List<FieldDefinition> safeArgs;

    private final List<FieldDefinition> unsafeArgs;

    private volatile int memoizedHashCode;

    private ErrorDefinition(
            TypeName errorName,
            Optional<Documentation> docs,
            ErrorNamespace namespace,
            ErrorCode code,
            List<FieldDefinition> safeArgs,
            List<FieldDefinition> unsafeArgs) {
        validateFields(errorName, docs, namespace, code, safeArgs, unsafeArgs);
        this.errorName = errorName;
        this.docs = docs;
        this.namespace = namespace;
        this.code = code;
        this.safeArgs = Collections.unmodifiableList(safeArgs);
        this.unsafeArgs = Collections.unmodifiableList(unsafeArgs);
    }

    @JsonProperty("errorName")
    public TypeName getErrorName() {
        return this.errorName;
    }

    @JsonProperty("docs")
    public Optional<Documentation> getDocs() {
        return this.docs;
    }

    @JsonProperty("namespace")
    public ErrorNamespace getNamespace() {
        return this.namespace;
    }

    @JsonProperty("code")
    public ErrorCode getCode() {
        return this.code;
    }

    @JsonProperty("safeArgs")
    public List<FieldDefinition> getSafeArgs() {
        return this.safeArgs;
    }

    @JsonProperty("unsafeArgs")
    public List<FieldDefinition> getUnsafeArgs() {
        return this.unsafeArgs;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || (other instanceof ErrorDefinition && equalTo((ErrorDefinition) other));
    }

    private boolean equalTo(ErrorDefinition other) {
        return this.errorName.equals(other.errorName)
                && this.docs.equals(other.docs)
                && this.namespace.equals(other.namespace)
                && this.code.equals(other.code)
                && this.safeArgs.equals(other.safeArgs)
                && this.unsafeArgs.equals(other.unsafeArgs);
    }

    @Override
    public int hashCode() {
        if (memoizedHashCode == 0) {
            memoizedHashCode = Objects.hash(errorName, docs, namespace, code, safeArgs, unsafeArgs);
        }
        return memoizedHashCode;
    }

    @Override
    public String toString() {
        return new StringBuilder("ErrorDefinition")
                .append("{")
                .append("errorName")
                .append(": ")
                .append(errorName)
                .append(", ")
                .append("docs")
                .append(": ")
                .append(docs)
                .append(", ")
                .append("namespace")
                .append(": ")
                .append(namespace)
                .append(", ")
                .append("code")
                .append(": ")
                .append(code)
                .append(", ")
                .append("safeArgs")
                .append(": ")
                .append(safeArgs)
                .append(", ")
                .append("unsafeArgs")
                .append(": ")
                .append(unsafeArgs)
                .append("}")
                .toString();
    }

    private static void validateFields(
            TypeName errorName,
            Optional<Documentation> docs,
            ErrorNamespace namespace,
            ErrorCode code,
            List<FieldDefinition> safeArgs,
            List<FieldDefinition> unsafeArgs) {
        List<String> missingFields = null;
        missingFields = addFieldIfMissing(missingFields, errorName, "errorName");
        missingFields = addFieldIfMissing(missingFields, docs, "docs");
        missingFields = addFieldIfMissing(missingFields, namespace, "namespace");
        missingFields = addFieldIfMissing(missingFields, code, "code");
        missingFields = addFieldIfMissing(missingFields, safeArgs, "safeArgs");
        missingFields = addFieldIfMissing(missingFields, unsafeArgs, "unsafeArgs");
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
                missingFields = new ArrayList<>(6);
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
        private TypeName errorName;

        private Optional<Documentation> docs = Optional.empty();

        private ErrorNamespace namespace;

        private ErrorCode code;

        private List<FieldDefinition> safeArgs = new ArrayList<>();

        private List<FieldDefinition> unsafeArgs = new ArrayList<>();

        private Builder() {}

        public Builder from(ErrorDefinition other) {
            errorName(other.getErrorName());
            docs(other.getDocs());
            namespace(other.getNamespace());
            code(other.getCode());
            safeArgs(other.getSafeArgs());
            unsafeArgs(other.getUnsafeArgs());
            return this;
        }

        @JsonSetter("errorName")
        public Builder errorName(TypeName errorName) {
            this.errorName = Objects.requireNonNull(errorName, "errorName cannot be null");
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

        @JsonSetter("namespace")
        public Builder namespace(ErrorNamespace namespace) {
            this.namespace = Objects.requireNonNull(namespace, "namespace cannot be null");
            return this;
        }

        @JsonSetter("code")
        public Builder code(ErrorCode code) {
            this.code = Objects.requireNonNull(code, "code cannot be null");
            return this;
        }

        @JsonSetter("safeArgs")
        public Builder safeArgs(Iterable<FieldDefinition> safeArgs) {
            this.safeArgs.clear();
            ConjureCollections.addAll(
                    this.safeArgs, Objects.requireNonNull(safeArgs, "safeArgs cannot be null"));
            return this;
        }

        public Builder addAllSafeArgs(Iterable<FieldDefinition> safeArgs) {
            ConjureCollections.addAll(
                    this.safeArgs, Objects.requireNonNull(safeArgs, "safeArgs cannot be null"));
            return this;
        }

        public Builder safeArgs(FieldDefinition safeArgs) {
            this.safeArgs.add(safeArgs);
            return this;
        }

        @JsonSetter("unsafeArgs")
        public Builder unsafeArgs(Iterable<FieldDefinition> unsafeArgs) {
            this.unsafeArgs.clear();
            ConjureCollections.addAll(
                    this.unsafeArgs,
                    Objects.requireNonNull(unsafeArgs, "unsafeArgs cannot be null"));
            return this;
        }

        public Builder addAllUnsafeArgs(Iterable<FieldDefinition> unsafeArgs) {
            ConjureCollections.addAll(
                    this.unsafeArgs,
                    Objects.requireNonNull(unsafeArgs, "unsafeArgs cannot be null"));
            return this;
        }

        public Builder unsafeArgs(FieldDefinition unsafeArgs) {
            this.unsafeArgs.add(unsafeArgs);
            return this;
        }

        public ErrorDefinition build() {
            return new ErrorDefinition(errorName, docs, namespace, code, safeArgs, unsafeArgs);
        }
    }
}
