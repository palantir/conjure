package com.palantir.conjure.spec;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Generated;

@JsonDeserialize(builder = CookieAuthType.Builder.class)
@Generated("com.palantir.conjure.gen.java.types.BeanGenerator")
public final class CookieAuthType {
    private final String cookieName;

    private volatile int memoizedHashCode;

    private CookieAuthType(String cookieName) {
        validateFields(cookieName);
        this.cookieName = cookieName;
    }

    @JsonProperty("cookieName")
    public String getCookieName() {
        return this.cookieName;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || (other instanceof CookieAuthType && equalTo((CookieAuthType) other));
    }

    private boolean equalTo(CookieAuthType other) {
        return this.cookieName.equals(other.cookieName);
    }

    @Override
    public int hashCode() {
        if (memoizedHashCode == 0) {
            memoizedHashCode = Objects.hash(cookieName);
        }
        return memoizedHashCode;
    }

    @Override
    public String toString() {
        return new StringBuilder("CookieAuthType")
                .append("{")
                .append("cookieName")
                .append(": ")
                .append(cookieName)
                .append("}")
                .toString();
    }

    public static CookieAuthType of(String cookieName) {
        return builder().cookieName(cookieName).build();
    }

    private static void validateFields(String cookieName) {
        List<String> missingFields = null;
        missingFields = addFieldIfMissing(missingFields, cookieName, "cookieName");
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
                missingFields = new ArrayList<>(1);
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
        private String cookieName;

        private Builder() {}

        public Builder from(CookieAuthType other) {
            cookieName(other.getCookieName());
            return this;
        }

        @JsonSetter("cookieName")
        public Builder cookieName(String cookieName) {
            this.cookieName = Objects.requireNonNull(cookieName, "cookieName cannot be null");
            return this;
        }

        public CookieAuthType build() {
            return new CookieAuthType(cookieName);
        }
    }
}
