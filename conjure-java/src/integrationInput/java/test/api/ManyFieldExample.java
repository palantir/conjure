package test.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Generated;

@JsonDeserialize(
        builder = ManyFieldExample.Builder.class
)
@JsonIgnoreProperties(
        ignoreUnknown = true
)
@Generated("com.palantir.conjure.gen.java.types.BeanGenerator")
public final class ManyFieldExample {
    private final String string;

    private final int integer;

    private final double doubleValue;

    private final Optional<String> optionalItem;

    private final List<String> items;

    private final Set<String> set;

    private final Map<String, String> map;

    private final StringAliasExample alias;

    private ManyFieldExample(@JsonProperty("string") String string, @JsonProperty("integer") int integer, @JsonProperty("doubleValue") double doubleValue, @JsonProperty("optionalItem") Optional<String> optionalItem, @JsonProperty("items") List<String> items, @JsonProperty("set") Set<String> set, @JsonProperty("map") Map<String, String> map, @JsonProperty("alias") StringAliasExample alias) {
        validateFields(string, optionalItem, items, set, map, alias);
        this.string = string;
        this.integer = integer;
        this.doubleValue = doubleValue;
        this.optionalItem = optionalItem;
        this.items = Collections.unmodifiableList(new ArrayList<>(items));
        this.set = Collections.unmodifiableSet(new LinkedHashSet<>(set));
        this.map = Collections.unmodifiableMap(new LinkedHashMap<>(map));
        this.alias = alias;
    }

    @JsonProperty("string")
    public String getString() {
        return this.string;
    }

    @JsonProperty("integer")
    public int getInteger() {
        return this.integer;
    }

    @JsonProperty("doubleValue")
    public double getDoubleValue() {
        return this.doubleValue;
    }

    @JsonProperty("optionalItem")
    public Optional<String> getOptionalItem() {
        return this.optionalItem;
    }

    @JsonProperty("items")
    public List<String> getItems() {
        return this.items;
    }

    @JsonProperty("set")
    public Set<String> getSet() {
        return this.set;
    }

    @JsonProperty("map")
    public Map<String, String> getMap() {
        return this.map;
    }

    @JsonProperty("alias")
    public StringAliasExample getAlias() {
        return this.alias;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof ManyFieldExample && equalTo((ManyFieldExample) other));
    }

    private boolean equalTo(ManyFieldExample other) {
        return this.string.equals(other.string) && this.integer == other.integer && this.doubleValue == other.doubleValue && this.optionalItem.equals(other.optionalItem) && this.items.equals(other.items) && this.set.equals(other.set) && this.map.equals(other.map) && this.alias.equals(other.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(string, integer, doubleValue, optionalItem, items, set, map, alias);
    }

    @Override
    public String toString() {
        return new StringBuilder("ManyFieldExample").append("{")
                .append("string").append(": ").append(string)
                .append(", ").append("integer").append(": ").append(integer)
                .append(", ").append("doubleValue").append(": ").append(doubleValue)
                .append(", ").append("optionalItem").append(": ").append(optionalItem)
                .append(", ").append("items").append(": ").append(items)
                .append(", ").append("set").append(": ").append(set)
                .append(", ").append("map").append(": ").append(map)
                .append(", ").append("alias").append(": ").append(alias)
            .append("}")
            .toString();
    }

    private static void validateFields(String string, Optional<String> optionalItem, List<String> items, Set<String> set, Map<String, String> map, StringAliasExample alias) {
        List<String> missingFields = null;
        missingFields = addFieldIfMissing(missingFields, string, "string");
        missingFields = addFieldIfMissing(missingFields, optionalItem, "optionalItem");
        missingFields = addFieldIfMissing(missingFields, items, "items");
        missingFields = addFieldIfMissing(missingFields, set, "set");
        missingFields = addFieldIfMissing(missingFields, map, "map");
        missingFields = addFieldIfMissing(missingFields, alias, "alias");
        if (missingFields != null) {
            throw new IllegalStateException("Some required fields have not been set: " + missingFields);
        }
    }

    private static List<String> addFieldIfMissing(List<String> prev, Object fieldValue, String fieldName) {
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

    public static final class Builder {
        private String string;

        private int integer;

        private double doubleValue;

        private Optional<String> optionalItem = Optional.empty();

        private List<String> items = new ArrayList<>();

        private Set<String> set = new LinkedHashSet<>();

        private Map<String, String> map = new LinkedHashMap<>();

        private StringAliasExample alias;

        private Builder() {
        }

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

        public Builder string(String string) {
            this.string = Objects.requireNonNull(string, "string cannot be null");
            return this;
        }

        public Builder integer(int integer) {
            this.integer = integer;
            return this;
        }

        public Builder doubleValue(double doubleValue) {
            this.doubleValue = doubleValue;
            return this;
        }

        public Builder optionalItem(Optional<String> optionalItem) {
            this.optionalItem = Objects.requireNonNull(optionalItem, "optionalItem cannot be null");
            return this;
        }

        public Builder optionalItem(String optionalItem) {
            this.optionalItem = Optional.of(Objects.requireNonNull(optionalItem, "optionalItem cannot be null"));
            return this;
        }

        public Builder items(Collection<String> items) {
            this.items.clear();
            this.items.addAll(Objects.requireNonNull(items, "items cannot be null"));
            return this;
        }

        public Builder addAllItems(Collection<String> items) {
            this.items.addAll(Objects.requireNonNull(items, "items cannot be null"));
            return this;
        }

        public Builder items(String items) {
            this.items.add(items);
            return this;
        }

        public Builder set(Collection<String> set) {
            this.set.clear();
            this.set.addAll(Objects.requireNonNull(set, "set cannot be null"));
            return this;
        }

        public Builder addAllSet(Collection<String> set) {
            this.set.addAll(Objects.requireNonNull(set, "set cannot be null"));
            return this;
        }

        public Builder set(String set) {
            this.set.add(set);
            return this;
        }

        public Builder map(Map<String, String> map) {
            this.map.clear();
            this.map.putAll(Objects.requireNonNull(map, "map cannot be null"));
            return this;
        }

        public Builder putAllMap(Map<String, String> map) {
            this.map.putAll(Objects.requireNonNull(map, "map cannot be null"));
            return this;
        }

        public Builder map(String key, String value) {
            this.map.put(key, value);
            return this;
        }

        public Builder alias(StringAliasExample alias) {
            this.alias = Objects.requireNonNull(alias, "alias cannot be null");
            return this;
        }

        public ManyFieldExample build() {
            return new ManyFieldExample(string, integer, doubleValue, optionalItem, items, set, map, alias);
        }
    }
}
