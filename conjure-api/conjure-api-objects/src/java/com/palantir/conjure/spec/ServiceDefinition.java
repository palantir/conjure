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

@JsonDeserialize(builder = ServiceDefinition.Builder.class)
@Generated("com.palantir.conjure.gen.java.types.BeanGenerator")
public final class ServiceDefinition {
    private final TypeName serviceName;

    private final List<EndpointDefinition> endpoints;

    private final Optional<Documentation> docs;

    private volatile int memoizedHashCode;

    private ServiceDefinition(
            TypeName serviceName,
            List<EndpointDefinition> endpoints,
            Optional<Documentation> docs) {
        validateFields(serviceName, endpoints, docs);
        this.serviceName = serviceName;
        this.endpoints = Collections.unmodifiableList(endpoints);
        this.docs = docs;
    }

    @JsonProperty("serviceName")
    public TypeName getServiceName() {
        return this.serviceName;
    }

    @JsonProperty("endpoints")
    public List<EndpointDefinition> getEndpoints() {
        return this.endpoints;
    }

    @JsonProperty("docs")
    public Optional<Documentation> getDocs() {
        return this.docs;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || (other instanceof ServiceDefinition && equalTo((ServiceDefinition) other));
    }

    private boolean equalTo(ServiceDefinition other) {
        return this.serviceName.equals(other.serviceName)
                && this.endpoints.equals(other.endpoints)
                && this.docs.equals(other.docs);
    }

    @Override
    public int hashCode() {
        if (memoizedHashCode == 0) {
            memoizedHashCode = Objects.hash(serviceName, endpoints, docs);
        }
        return memoizedHashCode;
    }

    @Override
    public String toString() {
        return new StringBuilder("ServiceDefinition")
                .append("{")
                .append("serviceName")
                .append(": ")
                .append(serviceName)
                .append(", ")
                .append("endpoints")
                .append(": ")
                .append(endpoints)
                .append(", ")
                .append("docs")
                .append(": ")
                .append(docs)
                .append("}")
                .toString();
    }

    public static ServiceDefinition of(
            TypeName serviceName, List<EndpointDefinition> endpoints, Documentation docs) {
        return builder()
                .serviceName(serviceName)
                .endpoints(endpoints)
                .docs(Optional.of(docs))
                .build();
    }

    private static void validateFields(
            TypeName serviceName,
            List<EndpointDefinition> endpoints,
            Optional<Documentation> docs) {
        List<String> missingFields = null;
        missingFields = addFieldIfMissing(missingFields, serviceName, "serviceName");
        missingFields = addFieldIfMissing(missingFields, endpoints, "endpoints");
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
        private TypeName serviceName;

        private List<EndpointDefinition> endpoints = new ArrayList<>();

        private Optional<Documentation> docs = Optional.empty();

        private Builder() {}

        public Builder from(ServiceDefinition other) {
            serviceName(other.getServiceName());
            endpoints(other.getEndpoints());
            docs(other.getDocs());
            return this;
        }

        @JsonSetter("serviceName")
        public Builder serviceName(TypeName serviceName) {
            this.serviceName = Objects.requireNonNull(serviceName, "serviceName cannot be null");
            return this;
        }

        @JsonSetter("endpoints")
        public Builder endpoints(Iterable<EndpointDefinition> endpoints) {
            this.endpoints.clear();
            ConjureCollections.addAll(
                    this.endpoints, Objects.requireNonNull(endpoints, "endpoints cannot be null"));
            return this;
        }

        public Builder addAllEndpoints(Iterable<EndpointDefinition> endpoints) {
            ConjureCollections.addAll(
                    this.endpoints, Objects.requireNonNull(endpoints, "endpoints cannot be null"));
            return this;
        }

        public Builder endpoints(EndpointDefinition endpoints) {
            this.endpoints.add(endpoints);
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

        public ServiceDefinition build() {
            return new ServiceDefinition(serviceName, endpoints, docs);
        }
    }
}
