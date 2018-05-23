package com.palantir.conjure.spec;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Generated;

@JsonDeserialize(builder = ExternalReference.Builder.class)
@Generated("com.palantir.conjure.gen.java.types.BeanGenerator")
public final class ExternalReference {
    private final TypeName externalReference;

    private final Type fallback;

    private volatile int memoizedHashCode;

    private ExternalReference(TypeName externalReference, Type fallback) {
        validateFields(externalReference, fallback);
        this.externalReference = externalReference;
        this.fallback = fallback;
    }

    /**
     * An identifier for a non-Conjure type which is already defined in a different language (e.g.
     * Java).
     */
    @JsonProperty("externalReference")
    public TypeName getExternalReference() {
        return this.externalReference;
    }

    /**
     * Other language generators may use the provided fallback if the non-Conjure type is not
     * available. The ANY PrimitiveType is permissible for all external types, but a more specific
     * definition is preferrable.
     */
    @JsonProperty("fallback")
    public Type getFallback() {
        return this.fallback;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || (other instanceof ExternalReference && equalTo((ExternalReference) other));
    }

    private boolean equalTo(ExternalReference other) {
        return this.externalReference.equals(other.externalReference)
                && this.fallback.equals(other.fallback);
    }

    @Override
    public int hashCode() {
        if (memoizedHashCode == 0) {
            memoizedHashCode = Objects.hash(externalReference, fallback);
        }
        return memoizedHashCode;
    }

    @Override
    public String toString() {
        return new StringBuilder("ExternalReference")
                .append("{")
                .append("externalReference")
                .append(": ")
                .append(externalReference)
                .append(", ")
                .append("fallback")
                .append(": ")
                .append(fallback)
                .append("}")
                .toString();
    }

    public static ExternalReference of(TypeName externalReference, Type fallback) {
        return builder().externalReference(externalReference).fallback(fallback).build();
    }

    private static void validateFields(TypeName externalReference, Type fallback) {
        List<String> missingFields = null;
        missingFields = addFieldIfMissing(missingFields, externalReference, "externalReference");
        missingFields = addFieldIfMissing(missingFields, fallback, "fallback");
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
                missingFields = new ArrayList<>(2);
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
        private TypeName externalReference;

        private Type fallback;

        private Builder() {}

        public Builder from(ExternalReference other) {
            externalReference(other.getExternalReference());
            fallback(other.getFallback());
            return this;
        }

        /**
         * An identifier for a non-Conjure type which is already defined in a different language
         * (e.g. Java).
         */
        @JsonSetter("externalReference")
        public Builder externalReference(TypeName externalReference) {
            this.externalReference =
                    Objects.requireNonNull(externalReference, "externalReference cannot be null");
            return this;
        }

        /**
         * Other language generators may use the provided fallback if the non-Conjure type is not
         * available. The ANY PrimitiveType is permissible for all external types, but a more
         * specific definition is preferrable.
         */
        @JsonSetter("fallback")
        public Builder fallback(Type fallback) {
            this.fallback = Objects.requireNonNull(fallback, "fallback cannot be null");
            return this;
        }

        public ExternalReference build() {
            return new ExternalReference(externalReference, fallback);
        }
    }
}
