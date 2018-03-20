package com.palantir.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Generated;

@JsonDeserialize(builder = UuidExample.Builder.class)
@Generated("com.palantir.conjure.gen.java.types.BeanGenerator")
public final class UuidExample implements Serializable {
    private static final long serialVersionUID = 1L;

    private final UUID uuid;

    private transient volatile int memoizedHashCode;

    private UuidExample(UUID uuid) {
        validateFields(uuid);
        this.uuid = uuid;
    }

    @JsonProperty("uuid")
    public UUID getUuid() {
        return this.uuid;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof UuidExample && equalTo((UuidExample) other));
    }

    private boolean equalTo(UuidExample other) {
        return this.uuid.equals(other.uuid);
    }

    @Override
    public int hashCode() {
        if (memoizedHashCode == 0) {
            memoizedHashCode = Objects.hash(uuid);
        }
        return memoizedHashCode;
    }

    @Override
    public String toString() {
        return new StringBuilder("UuidExample")
                .append("{")
                .append("uuid")
                .append(": ")
                .append(uuid)
                .append("}")
                .toString();
    }

    public static UuidExample of(UUID uuid) {
        return builder().uuid(uuid).build();
    }

    private static void validateFields(UUID uuid) {
        List<String> missingFields = null;
        missingFields = addFieldIfMissing(missingFields, uuid, "uuid");
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
        private UUID uuid;

        private Builder() {}

        public Builder from(UuidExample other) {
            uuid(other.getUuid());
            return this;
        }

        @JsonSetter("uuid")
        public Builder uuid(UUID uuid) {
            this.uuid = Objects.requireNonNull(uuid, "uuid cannot be null");
            return this;
        }

        public UuidExample build() {
            return new UuidExample(uuid);
        }
    }
}
