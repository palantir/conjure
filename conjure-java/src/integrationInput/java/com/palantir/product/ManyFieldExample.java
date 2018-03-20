package com.palantir.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.conjure.lib.internal.ConjureCollections;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Generated;

@JsonDeserialize(builder = ManyFieldExample.Builder.class)
@Generated("com.palantir.conjure.gen.java.types.BeanGenerator")
public final class ManyFieldExample implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String string;

    private final int integer;

    private final double doubleValue;

    private final Optional<String> optionalItem;

    private final List<String> items;

    private final Set<String> set;

    private final Map<String, String> map;

    private final StringAliasExample alias;

    private transient volatile int memoizedHashCode;

    private ManyFieldExample(
            String string,
            int integer,
            double doubleValue,
            Optional<String> optionalItem,
            List<String> items,
            Set<String> set,
            Map<String, String> map,
            StringAliasExample alias) {
        validateFields(string, optionalItem, items, set, map, alias);
        this.string = string;
        this.integer = integer;
        this.doubleValue = doubleValue;
        this.optionalItem = optionalItem;
        this.items = Collections.unmodifiableList(items);
        this.set = Collections.unmodifiableSet(set);
        this.map = Collections.unmodifiableMap(map);
        this.alias = alias;
    }

    /** docs for string field */
    @JsonProperty("string")
    public String getString() {
        return this.string;
    }

    /** docs for integer field */
    @JsonProperty("integer")
    public int getInteger() {
        return this.integer;
    }

    /** docs for doubleValue field */
    @JsonProperty("doubleValue")
    public double getDoubleValue() {
        return this.doubleValue;
    }

    /** docs for optionalItem field */
    @JsonProperty("optionalItem")
    public Optional<String> getOptionalItem() {
        return this.optionalItem;
    }

    /** docs for items field */
    @JsonProperty("items")
    public List<String> getItems() {
        return this.items;
    }

    /** docs for set field */
    @JsonProperty("set")
    public Set<String> getSet() {
        return this.set;
    }

    /** docs for map field */
    @JsonProperty("map")
    public Map<String, String> getMap() {
        return this.map;
    }

    /** docs for alias field */
    @JsonProperty("alias")
    public StringAliasExample getAlias() {
        return this.alias;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || (other instanceof ManyFieldExample && equalTo((ManyFieldExample) other));
    }

    private boolean equalTo(ManyFieldExample other) {
        return this.string.equals(other.string)
                && this.integer == other.integer
                && this.doubleValue == other.doubleValue
                && this.optionalItem.equals(other.optionalItem)
                && this.items.equals(other.items)
                && this.set.equals(other.set)
                && this.map.equals(other.map)
                && this.alias.equals(other.alias);
    }

    @Override
    public int hashCode() {
        if (memoizedHashCode == 0) {
            memoizedHashCode =
                    Objects.hash(
                            string, integer, doubleValue, optionalItem, items, set, map, alias);
        }
        return memoizedHashCode;
    }

    @Override
    public String toString() {
        return new StringBuilder("ManyFieldExample")
                .append("{")
                .append("string")
                .append(": ")
                .append(string)
                .append(", ")
                .append("integer")
                .append(": ")
                .append(integer)
                .append(", ")
                .append("doubleValue")
                .append(": ")
                .append(doubleValue)
                .append(", ")
                .append("optionalItem")
                .append(": ")
                .append(optionalItem)
                .append(", ")
                .append("items")
                .append(": ")
                .append(items)
                .append(", ")
                .append("set")
                .append(": ")
                .append(set)
                .append(", ")
                .append("map")
                .append(": ")
                .append(map)
                .append(", ")
                .append("alias")
                .append(": ")
                .append(alias)
                .append("}")
                .toString();
    }

    private static void validateFields(
            String string,
            Optional<String> optionalItem,
            List<String> items,
            Set<String> set,
            Map<String, String> map,
            StringAliasExample alias) {
        List<String> missingFields = null;
        missingFields = addFieldIfMissing(missingFields, string, "string");
        missingFields = addFieldIfMissing(missingFields, optionalItem, "optionalItem");
        missingFields = addFieldIfMissing(missingFields, items, "items");
        missingFields = addFieldIfMissing(missingFields, set, "set");
        missingFields = addFieldIfMissing(missingFields, map, "map");
        missingFields = addFieldIfMissing(missingFields, alias, "alias");
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
                missingFields = new ArrayList<>(6);
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
        private String string;

        private int integer;

        private double doubleValue;

        private Optional<String> optionalItem = Optional.empty();

        private List<String> items = new ArrayList<>();

        private Set<String> set = new LinkedHashSet<>();

        private Map<String, String> map = new LinkedHashMap<>();

        private StringAliasExample alias;

        private Builder() {}

        public Builder from(ManyFieldExample other) {
            string(other.getString());
            integer(other.getInteger());
            doubleValue(other.getDoubleValue());
            optionalItem(other.getOptionalItem());
            items(other.getItems());
            set(other.getSet());
            map(other.getMap());
            alias(other.getAlias());
            return this;
        }

        /** docs for string field */
        @JsonSetter("string")
        public Builder string(String string) {
            this.string = Objects.requireNonNull(string, "string cannot be null");
            return this;
        }

        /** docs for integer field */
        @JsonSetter("integer")
        public Builder integer(int integer) {
            this.integer = integer;
            return this;
        }

        /** docs for doubleValue field */
        @JsonSetter("doubleValue")
        public Builder doubleValue(double doubleValue) {
            this.doubleValue = doubleValue;
            return this;
        }

        /** docs for optionalItem field */
        @JsonSetter("optionalItem")
        public Builder optionalItem(Optional<String> optionalItem) {
            this.optionalItem = Objects.requireNonNull(optionalItem, "optionalItem cannot be null");
            return this;
        }

        /** docs for optionalItem field */
        public Builder optionalItem(String optionalItem) {
            this.optionalItem =
                    Optional.of(
                            Objects.requireNonNull(optionalItem, "optionalItem cannot be null"));
            return this;
        }

        /** docs for items field */
        @JsonSetter("items")
        public Builder items(Iterable<String> items) {
            this.items.clear();
            ConjureCollections.addAll(
                    this.items, Objects.requireNonNull(items, "items cannot be null"));
            return this;
        }

        /** docs for items field */
        public Builder addAllItems(Iterable<String> items) {
            ConjureCollections.addAll(
                    this.items, Objects.requireNonNull(items, "items cannot be null"));
            return this;
        }

        /** docs for items field */
        public Builder items(String items) {
            this.items.add(items);
            return this;
        }

        /** docs for set field */
        @JsonSetter("set")
        public Builder set(Iterable<String> set) {
            this.set.clear();
            ConjureCollections.addAll(this.set, Objects.requireNonNull(set, "set cannot be null"));
            return this;
        }

        /** docs for set field */
        public Builder addAllSet(Iterable<String> set) {
            ConjureCollections.addAll(this.set, Objects.requireNonNull(set, "set cannot be null"));
            return this;
        }

        /** docs for set field */
        public Builder set(String set) {
            this.set.add(set);
            return this;
        }

        /** docs for map field */
        @JsonSetter("map")
        public Builder map(Map<String, String> map) {
            this.map.clear();
            this.map.putAll(Objects.requireNonNull(map, "map cannot be null"));
            return this;
        }

        /** docs for map field */
        public Builder putAllMap(Map<String, String> map) {
            this.map.putAll(Objects.requireNonNull(map, "map cannot be null"));
            return this;
        }

        /** docs for map field */
        public Builder map(String key, String value) {
            this.map.put(key, value);
            return this;
        }

        /** docs for alias field */
        @JsonSetter("alias")
        public Builder alias(StringAliasExample alias) {
            this.alias = Objects.requireNonNull(alias, "alias cannot be null");
            return this;
        }

        public ManyFieldExample build() {
            return new ManyFieldExample(
                    string, integer, doubleValue, optionalItem, items, set, map, alias);
        }
    }
}
