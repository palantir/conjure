package com.palantir.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.conjure.lib.internal.ConjureCollections;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Generated;

@JsonDeserialize(builder = SetExample.Builder.class)
@Generated("com.palantir.conjure.gen.java.types.BeanGenerator")
public final class SetExample implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Set<String> items;

    private SetExample(Set<String> items) {
        validateFields(items);
        this.items = Collections.unmodifiableSet(items);
    }

    @JsonProperty("items")
    public Set<String> getItems() {
        return this.items;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof SetExample && equalTo((SetExample) other));
    }

    private boolean equalTo(SetExample other) {
        return this.items.equals(other.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(items);
    }

    @Override
    public String toString() {
        return new StringBuilder("SetExample")
                .append("{")
                .append("items")
                .append(": ")
                .append(items)
                .append("}")
                .toString();
    }

    public static SetExample of(Set<String> items) {
        return builder().items(items).build();
    }

    private static void validateFields(Set<String> items) {
        List<String> missingFields = null;
        missingFields = addFieldIfMissing(missingFields, items, "items");
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
        private Set<String> items = new LinkedHashSet<>();

        private Builder() {}

        public Builder from(SetExample other) {
            items(other.getItems());
            return this;
        }

        @JsonSetter("items")
        public Builder items(Iterable<String> items) {
            this.items.clear();
            ConjureCollections.addAll(
                    this.items, Objects.requireNonNull(items, "items cannot be null"));
            return this;
        }

        public Builder addAllItems(Iterable<String> items) {
            ConjureCollections.addAll(
                    this.items, Objects.requireNonNull(items, "items cannot be null"));
            return this;
        }

        public Builder items(String items) {
            this.items.add(items);
            return this;
        }

        public SetExample build() {
            return new SetExample(items);
        }
    }
}
