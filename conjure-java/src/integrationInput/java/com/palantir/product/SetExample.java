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

    private final Set<Double> doubleItems;

    private transient volatile int memoizedHashCode;

    private SetExample(Set<String> items, Set<Double> doubleItems) {
        validateFields(items, doubleItems);
        this.items = Collections.unmodifiableSet(items);
        this.doubleItems = Collections.unmodifiableSet(doubleItems);
    }

    @JsonProperty("items")
    public Set<String> getItems() {
        return this.items;
    }

    @JsonProperty("doubleItems")
    public Set<Double> getDoubleItems() {
        return this.doubleItems;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof SetExample && equalTo((SetExample) other));
    }

    private boolean equalTo(SetExample other) {
        return this.items.equals(other.items) && this.doubleItems.equals(other.doubleItems);
    }

    @Override
    public int hashCode() {
        if (memoizedHashCode == 0) {
            memoizedHashCode = Objects.hash(items, doubleItems);
        }
        return memoizedHashCode;
    }

    @Override
    public String toString() {
        return new StringBuilder("SetExample")
                .append("{")
                .append("items")
                .append(": ")
                .append(items)
                .append(", ")
                .append("doubleItems")
                .append(": ")
                .append(doubleItems)
                .append("}")
                .toString();
    }

    public static SetExample of(Set<String> items, Set<Double> doubleItems) {
        return builder().items(items).doubleItems(doubleItems).build();
    }

    private static void validateFields(Set<String> items, Set<Double> doubleItems) {
        List<String> missingFields = null;
        missingFields = addFieldIfMissing(missingFields, items, "items");
        missingFields = addFieldIfMissing(missingFields, doubleItems, "doubleItems");
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
        private Set<String> items = new LinkedHashSet<>();

        private Set<Double> doubleItems = new LinkedHashSet<>();

        private Builder() {}

        public Builder from(SetExample other) {
            items(other.getItems());
            doubleItems(other.getDoubleItems());
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

        @JsonSetter("doubleItems")
        public Builder doubleItems(Iterable<Double> doubleItems) {
            this.doubleItems.clear();
            ConjureCollections.addAll(
                    this.doubleItems,
                    Objects.requireNonNull(doubleItems, "doubleItems cannot be null"));
            return this;
        }

        public Builder addAllDoubleItems(Iterable<Double> doubleItems) {
            ConjureCollections.addAll(
                    this.doubleItems,
                    Objects.requireNonNull(doubleItems, "doubleItems cannot be null"));
            return this;
        }

        public Builder doubleItems(double doubleItems) {
            this.doubleItems.add(doubleItems);
            return this;
        }

        public SetExample build() {
            return new SetExample(items, doubleItems);
        }
    }
}
