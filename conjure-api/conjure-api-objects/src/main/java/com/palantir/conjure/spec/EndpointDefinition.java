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

@JsonDeserialize(builder = EndpointDefinition.Builder.class)
@Generated("com.palantir.conjure.gen.java.types.BeanGenerator")
public final class EndpointDefinition {
    private final EndpointName endpointName;

    private final HttpMethod httpMethod;

    private final HttpPath httpPath;

    private final Optional<AuthType> auth;

    private final List<ArgumentDefinition> args;

    private final Optional<Type> returns;

    private final Optional<Documentation> docs;

    private final Optional<Documentation> deprecated;

    private final List<Type> markers;

    private volatile int memoizedHashCode;

    private EndpointDefinition(
            EndpointName endpointName,
            HttpMethod httpMethod,
            HttpPath httpPath,
            Optional<AuthType> auth,
            List<ArgumentDefinition> args,
            Optional<Type> returns,
            Optional<Documentation> docs,
            Optional<Documentation> deprecated,
            List<Type> markers) {
        validateFields(
                endpointName, httpMethod, httpPath, auth, args, returns, docs, deprecated, markers);
        this.endpointName = endpointName;
        this.httpMethod = httpMethod;
        this.httpPath = httpPath;
        this.auth = auth;
        this.args = Collections.unmodifiableList(args);
        this.returns = returns;
        this.docs = docs;
        this.deprecated = deprecated;
        this.markers = Collections.unmodifiableList(markers);
    }

    @JsonProperty("endpointName")
    public EndpointName getEndpointName() {
        return this.endpointName;
    }

    @JsonProperty("httpMethod")
    public HttpMethod getHttpMethod() {
        return this.httpMethod;
    }

    @JsonProperty("httpPath")
    public HttpPath getHttpPath() {
        return this.httpPath;
    }

    @JsonProperty("auth")
    public Optional<AuthType> getAuth() {
        return this.auth;
    }

    @JsonProperty("args")
    public List<ArgumentDefinition> getArgs() {
        return this.args;
    }

    @JsonProperty("returns")
    public Optional<Type> getReturns() {
        return this.returns;
    }

    @JsonProperty("docs")
    public Optional<Documentation> getDocs() {
        return this.docs;
    }

    @JsonProperty("deprecated")
    public Optional<Documentation> getDeprecated() {
        return this.deprecated;
    }

    @JsonProperty("markers")
    public List<Type> getMarkers() {
        return this.markers;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || (other instanceof EndpointDefinition && equalTo((EndpointDefinition) other));
    }

    private boolean equalTo(EndpointDefinition other) {
        return this.endpointName.equals(other.endpointName)
                && this.httpMethod.equals(other.httpMethod)
                && this.httpPath.equals(other.httpPath)
                && this.auth.equals(other.auth)
                && this.args.equals(other.args)
                && this.returns.equals(other.returns)
                && this.docs.equals(other.docs)
                && this.deprecated.equals(other.deprecated)
                && this.markers.equals(other.markers);
    }

    @Override
    public int hashCode() {
        if (memoizedHashCode == 0) {
            memoizedHashCode =
                    Objects.hash(
                            endpointName,
                            httpMethod,
                            httpPath,
                            auth,
                            args,
                            returns,
                            docs,
                            deprecated,
                            markers);
        }
        return memoizedHashCode;
    }

    @Override
    public String toString() {
        return new StringBuilder("EndpointDefinition")
                .append("{")
                .append("endpointName")
                .append(": ")
                .append(endpointName)
                .append(", ")
                .append("httpMethod")
                .append(": ")
                .append(httpMethod)
                .append(", ")
                .append("httpPath")
                .append(": ")
                .append(httpPath)
                .append(", ")
                .append("auth")
                .append(": ")
                .append(auth)
                .append(", ")
                .append("args")
                .append(": ")
                .append(args)
                .append(", ")
                .append("returns")
                .append(": ")
                .append(returns)
                .append(", ")
                .append("docs")
                .append(": ")
                .append(docs)
                .append(", ")
                .append("deprecated")
                .append(": ")
                .append(deprecated)
                .append(", ")
                .append("markers")
                .append(": ")
                .append(markers)
                .append("}")
                .toString();
    }

    private static void validateFields(
            EndpointName endpointName,
            HttpMethod httpMethod,
            HttpPath httpPath,
            Optional<AuthType> auth,
            List<ArgumentDefinition> args,
            Optional<Type> returns,
            Optional<Documentation> docs,
            Optional<Documentation> deprecated,
            List<Type> markers) {
        List<String> missingFields = null;
        missingFields = addFieldIfMissing(missingFields, endpointName, "endpointName");
        missingFields = addFieldIfMissing(missingFields, httpMethod, "httpMethod");
        missingFields = addFieldIfMissing(missingFields, httpPath, "httpPath");
        missingFields = addFieldIfMissing(missingFields, auth, "auth");
        missingFields = addFieldIfMissing(missingFields, args, "args");
        missingFields = addFieldIfMissing(missingFields, returns, "returns");
        missingFields = addFieldIfMissing(missingFields, docs, "docs");
        missingFields = addFieldIfMissing(missingFields, deprecated, "deprecated");
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
                missingFields = new ArrayList<>(9);
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
        private EndpointName endpointName;

        private HttpMethod httpMethod;

        private HttpPath httpPath;

        private Optional<AuthType> auth = Optional.empty();

        private List<ArgumentDefinition> args = new ArrayList<>();

        private Optional<Type> returns = Optional.empty();

        private Optional<Documentation> docs = Optional.empty();

        private Optional<Documentation> deprecated = Optional.empty();

        private List<Type> markers = new ArrayList<>();

        private Builder() {}

        public Builder from(EndpointDefinition other) {
            endpointName(other.getEndpointName());
            httpMethod(other.getHttpMethod());
            httpPath(other.getHttpPath());
            auth(other.getAuth());
            args(other.getArgs());
            returns(other.getReturns());
            docs(other.getDocs());
            deprecated(other.getDeprecated());
            markers(other.getMarkers());
            return this;
        }

        @JsonSetter("endpointName")
        public Builder endpointName(EndpointName endpointName) {
            this.endpointName = Objects.requireNonNull(endpointName, "endpointName cannot be null");
            return this;
        }

        @JsonSetter("httpMethod")
        public Builder httpMethod(HttpMethod httpMethod) {
            this.httpMethod = Objects.requireNonNull(httpMethod, "httpMethod cannot be null");
            return this;
        }

        @JsonSetter("httpPath")
        public Builder httpPath(HttpPath httpPath) {
            this.httpPath = Objects.requireNonNull(httpPath, "httpPath cannot be null");
            return this;
        }

        @JsonSetter("auth")
        public Builder auth(Optional<AuthType> auth) {
            this.auth = Objects.requireNonNull(auth, "auth cannot be null");
            return this;
        }

        public Builder auth(AuthType auth) {
            this.auth = Optional.of(Objects.requireNonNull(auth, "auth cannot be null"));
            return this;
        }

        @JsonSetter("args")
        public Builder args(Iterable<ArgumentDefinition> args) {
            this.args.clear();
            ConjureCollections.addAll(
                    this.args, Objects.requireNonNull(args, "args cannot be null"));
            return this;
        }

        public Builder addAllArgs(Iterable<ArgumentDefinition> args) {
            ConjureCollections.addAll(
                    this.args, Objects.requireNonNull(args, "args cannot be null"));
            return this;
        }

        public Builder args(ArgumentDefinition args) {
            this.args.add(args);
            return this;
        }

        @JsonSetter("returns")
        public Builder returns(Optional<Type> returns) {
            this.returns = Objects.requireNonNull(returns, "returns cannot be null");
            return this;
        }

        public Builder returns(Type returns) {
            this.returns = Optional.of(Objects.requireNonNull(returns, "returns cannot be null"));
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

        @JsonSetter("deprecated")
        public Builder deprecated(Optional<Documentation> deprecated) {
            this.deprecated = Objects.requireNonNull(deprecated, "deprecated cannot be null");
            return this;
        }

        public Builder deprecated(Documentation deprecated) {
            this.deprecated =
                    Optional.of(Objects.requireNonNull(deprecated, "deprecated cannot be null"));
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

        public EndpointDefinition build() {
            return new EndpointDefinition(
                    endpointName,
                    httpMethod,
                    httpPath,
                    auth,
                    args,
                    returns,
                    docs,
                    deprecated,
                    markers);
        }
    }
}
