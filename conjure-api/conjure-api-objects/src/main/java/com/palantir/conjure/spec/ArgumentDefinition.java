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

@JsonDeserialize(builder = ArgumentDefinition.Builder.class)
@Generated("com.palantir.conjure.gen.java.types.BeanGenerator")
public final class ArgumentDefinition {
    private final ArgumentName argName;

    private final Type type;

    private final ParameterType paramType;

    private final Optional<Documentation> docs;

    private final List<Type> markers;

    private volatile int memoizedHashCode;

    private ArgumentDefinition(
            ArgumentName argName,
            Type type,
            ParameterType paramType,
            Optional<Documentation> docs,
            List<Type> markers) {
        validateFields(argName, type, paramType, docs, markers);
        this.argName = argName;
        this.type = type;
        this.paramType = paramType;
        this.docs = docs;
        this.markers = Collections.unmodifiableList(markers);
    }

    @JsonProperty("argName")
    public ArgumentName getArgName() {
        return this.argName;
    }

    @JsonProperty("type")
    public Type getType() {
        return this.type;
    }

    @JsonProperty("paramType")
    public ParameterType getParamType() {
        return this.paramType;
    }

    @JsonProperty("docs")
    public Optional<Documentation> getDocs() {
        return this.docs;
    }

    @JsonProperty("markers")
    public List<Type> getMarkers() {
        return this.markers;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || (other instanceof ArgumentDefinition && equalTo((ArgumentDefinition) other));
    }

    private boolean equalTo(ArgumentDefinition other) {
        return this.argName.equals(other.argName)
                && this.type.equals(other.type)
                && this.paramType.equals(other.paramType)
                && this.docs.equals(other.docs)
                && this.markers.equals(other.markers);
    }

    @Override
    public int hashCode() {
        if (memoizedHashCode == 0) {
            memoizedHashCode = Objects.hash(argName, type, paramType, docs, markers);
        }
        return memoizedHashCode;
    }

    @Override
    public String toString() {
        return new StringBuilder("ArgumentDefinition")
                .append("{")
                .append("argName")
                .append(": ")
                .append(argName)
                .append(", ")
                .append("type")
                .append(": ")
                .append(type)
                .append(", ")
                .append("paramType")
                .append(": ")
                .append(paramType)
                .append(", ")
                .append("docs")
                .append(": ")
                .append(docs)
                .append(", ")
                .append("markers")
                .append(": ")
                .append(markers)
                .append("}")
                .toString();
    }

    private static void validateFields(
            ArgumentName argName,
            Type type,
            ParameterType paramType,
            Optional<Documentation> docs,
            List<Type> markers) {
        List<String> missingFields = null;
        missingFields = addFieldIfMissing(missingFields, argName, "argName");
        missingFields = addFieldIfMissing(missingFields, type, "type");
        missingFields = addFieldIfMissing(missingFields, paramType, "paramType");
        missingFields = addFieldIfMissing(missingFields, docs, "docs");
        missingFields = addFieldIfMissing(missingFields, markers, "markers");
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
                missingFields = new ArrayList<>(5);
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
        private ArgumentName argName;

        private Type type;

        private ParameterType paramType;

        private Optional<Documentation> docs = Optional.empty();

        private List<Type> markers = new ArrayList<>();

        private Builder() {}

        public Builder from(ArgumentDefinition other) {
            argName(other.getArgName());
            type(other.getType());
            paramType(other.getParamType());
            docs(other.getDocs());
            markers(other.getMarkers());
            return this;
        }

        @JsonSetter("argName")
        public Builder argName(ArgumentName argName) {
            this.argName = Objects.requireNonNull(argName, "argName cannot be null");
            return this;
        }

        @JsonSetter("type")
        public Builder type(Type type) {
            this.type = Objects.requireNonNull(type, "type cannot be null");
            return this;
        }

        @JsonSetter("paramType")
        public Builder paramType(ParameterType paramType) {
            this.paramType = Objects.requireNonNull(paramType, "paramType cannot be null");
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

        @JsonSetter("markers")
        public Builder markers(Iterable<Type> markers) {
            this.markers.clear();
            ConjureCollections.addAll(
                    this.markers, Objects.requireNonNull(markers, "markers cannot be null"));
            return this;
        }

        public Builder addAllMarkers(Iterable<Type> markers) {
            ConjureCollections.addAll(
                    this.markers, Objects.requireNonNull(markers, "markers cannot be null"));
            return this;
        }

        public Builder markers(Type markers) {
            this.markers.add(markers);
            return this;
        }

        public ArgumentDefinition build() {
            return new ArgumentDefinition(argName, type, paramType, docs, markers);
        }
    }
}
