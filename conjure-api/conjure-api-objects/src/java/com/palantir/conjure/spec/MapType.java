package com.palantir.conjure.spec;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Generated;

@JsonDeserialize(builder = MapType.Builder.class)
@Generated("com.palantir.conjure.gen.java.types.BeanGenerator")
public final class MapType {
    private final Type keyType;

    private final Type valueType;

    private volatile int memoizedHashCode;

    private MapType(Type keyType, Type valueType) {
        validateFields(keyType, valueType);
        this.keyType = keyType;
        this.valueType = valueType;
    }

    @JsonProperty("keyType")
    public Type getKeyType() {
        return this.keyType;
    }

    @JsonProperty("valueType")
    public Type getValueType() {
        return this.valueType;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof MapType && equalTo((MapType) other));
    }

    private boolean equalTo(MapType other) {
        return this.keyType.equals(other.keyType) && this.valueType.equals(other.valueType);
    }

    @Override
    public int hashCode() {
        if (memoizedHashCode == 0) {
            memoizedHashCode = Objects.hash(keyType, valueType);
        }
        return memoizedHashCode;
    }

    @Override
    public String toString() {
        return new StringBuilder("MapType")
                .append("{")
                .append("keyType")
                .append(": ")
                .append(keyType)
                .append(", ")
                .append("valueType")
                .append(": ")
                .append(valueType)
                .append("}")
                .toString();
    }

    public static MapType of(Type keyType, Type valueType) {
        return builder().keyType(keyType).valueType(valueType).build();
    }

    private static void validateFields(Type keyType, Type valueType) {
        List<String> missingFields = null;
        missingFields = addFieldIfMissing(missingFields, keyType, "keyType");
        missingFields = addFieldIfMissing(missingFields, valueType, "valueType");
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
        private Type keyType;

        private Type valueType;

        private Builder() {}

        public Builder from(MapType other) {
            keyType(other.getKeyType());
            valueType(other.getValueType());
            return this;
        }

        @JsonSetter("keyType")
        public Builder keyType(Type keyType) {
            this.keyType = Objects.requireNonNull(keyType, "keyType cannot be null");
            return this;
        }

        @JsonSetter("valueType")
        public Builder valueType(Type valueType) {
            this.valueType = Objects.requireNonNull(valueType, "valueType cannot be null");
            return this;
        }

        public MapType build() {
            return new MapType(keyType, valueType);
        }
    }
}
