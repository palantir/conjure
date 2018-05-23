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
import javax.annotation.Generated;

@JsonDeserialize(builder = ConjureDefinition.Builder.class)
@Generated("com.palantir.conjure.gen.java.types.BeanGenerator")
public final class ConjureDefinition {
    private final int version;

    private final List<ErrorDefinition> errors;

    private final List<TypeDefinition> types;

    private final List<ServiceDefinition> services;

    private volatile int memoizedHashCode;

    private ConjureDefinition(
            int version,
            List<ErrorDefinition> errors,
            List<TypeDefinition> types,
            List<ServiceDefinition> services) {
        validateFields(errors, types, services);
        this.version = version;
        this.errors = Collections.unmodifiableList(errors);
        this.types = Collections.unmodifiableList(types);
        this.services = Collections.unmodifiableList(services);
    }

    @JsonProperty("version")
    public int getVersion() {
        return this.version;
    }

    @JsonProperty("errors")
    public List<ErrorDefinition> getErrors() {
        return this.errors;
    }

    @JsonProperty("types")
    public List<TypeDefinition> getTypes() {
        return this.types;
    }

    @JsonProperty("services")
    public List<ServiceDefinition> getServices() {
        return this.services;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || (other instanceof ConjureDefinition && equalTo((ConjureDefinition) other));
    }

    private boolean equalTo(ConjureDefinition other) {
        return this.version == other.version
                && this.errors.equals(other.errors)
                && this.types.equals(other.types)
                && this.services.equals(other.services);
    }

    @Override
    public int hashCode() {
        if (memoizedHashCode == 0) {
            memoizedHashCode = Objects.hash(version, errors, types, services);
        }
        return memoizedHashCode;
    }

    @Override
    public String toString() {
        return new StringBuilder("ConjureDefinition")
                .append("{")
                .append("version")
                .append(": ")
                .append(version)
                .append(", ")
                .append("errors")
                .append(": ")
                .append(errors)
                .append(", ")
                .append("types")
                .append(": ")
                .append(types)
                .append(", ")
                .append("services")
                .append(": ")
                .append(services)
                .append("}")
                .toString();
    }

    private static void validateFields(
            List<ErrorDefinition> errors,
            List<TypeDefinition> types,
            List<ServiceDefinition> services) {
        List<String> missingFields = null;
        missingFields = addFieldIfMissing(missingFields, errors, "errors");
        missingFields = addFieldIfMissing(missingFields, types, "types");
        missingFields = addFieldIfMissing(missingFields, services, "services");
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
        private int version;

        private List<ErrorDefinition> errors = new ArrayList<>();

        private List<TypeDefinition> types = new ArrayList<>();

        private List<ServiceDefinition> services = new ArrayList<>();

        private Builder() {}

        public Builder from(ConjureDefinition other) {
            version(other.getVersion());
            errors(other.getErrors());
            types(other.getTypes());
            services(other.getServices());
            return this;
        }

        @JsonSetter("version")
        public Builder version(int version) {
            this.version = version;
            return this;
        }

        @JsonSetter("errors")
        public Builder errors(Iterable<ErrorDefinition> errors) {
            this.errors.clear();
            ConjureCollections.addAll(
                    this.errors, Objects.requireNonNull(errors, "errors cannot be null"));
            return this;
        }

        public Builder addAllErrors(Iterable<ErrorDefinition> errors) {
            ConjureCollections.addAll(
                    this.errors, Objects.requireNonNull(errors, "errors cannot be null"));
            return this;
        }

        public Builder errors(ErrorDefinition errors) {
            this.errors.add(errors);
            return this;
        }

        @JsonSetter("types")
        public Builder types(Iterable<TypeDefinition> types) {
            this.types.clear();
            ConjureCollections.addAll(
                    this.types, Objects.requireNonNull(types, "types cannot be null"));
            return this;
        }

        public Builder addAllTypes(Iterable<TypeDefinition> types) {
            ConjureCollections.addAll(
                    this.types, Objects.requireNonNull(types, "types cannot be null"));
            return this;
        }

        public Builder types(TypeDefinition types) {
            this.types.add(types);
            return this;
        }

        @JsonSetter("services")
        public Builder services(Iterable<ServiceDefinition> services) {
            this.services.clear();
            ConjureCollections.addAll(
                    this.services, Objects.requireNonNull(services, "services cannot be null"));
            return this;
        }

        public Builder addAllServices(Iterable<ServiceDefinition> services) {
            ConjureCollections.addAll(
                    this.services, Objects.requireNonNull(services, "services cannot be null"));
            return this;
        }

        public Builder services(ServiceDefinition services) {
            this.services.add(services);
            return this;
        }

        public ConjureDefinition build() {
            return new ConjureDefinition(version, errors, types, services);
        }
    }
}
