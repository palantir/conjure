package com.palantir.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Generated;

@JsonDeserialize(builder = AliasAsMapKeyExample.Builder.class)
@Generated("com.palantir.conjure.gen.java.types.BeanGenerator")
public final class AliasAsMapKeyExample implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<StringAliasExample, ManyFieldExample> strings;

    private final Map<RidAliasExample, ManyFieldExample> rids;

    private final Map<BearerTokenAliasExample, ManyFieldExample> bearertokens;

    private final Map<IntegerAliasExample, ManyFieldExample> integers;

    private final Map<SafeLongAliasExample, ManyFieldExample> safelongs;

    private final Map<DateTimeAliasExample, ManyFieldExample> datetimes;

    private final Map<UuidAliasExample, ManyFieldExample> uuids;

    private transient volatile int memoizedHashCode;

    private AliasAsMapKeyExample(
            Map<StringAliasExample, ManyFieldExample> strings,
            Map<RidAliasExample, ManyFieldExample> rids,
            Map<BearerTokenAliasExample, ManyFieldExample> bearertokens,
            Map<IntegerAliasExample, ManyFieldExample> integers,
            Map<SafeLongAliasExample, ManyFieldExample> safelongs,
            Map<DateTimeAliasExample, ManyFieldExample> datetimes,
            Map<UuidAliasExample, ManyFieldExample> uuids) {
        validateFields(strings, rids, bearertokens, integers, safelongs, datetimes, uuids);
        this.strings = Collections.unmodifiableMap(strings);
        this.rids = Collections.unmodifiableMap(rids);
        this.bearertokens = Collections.unmodifiableMap(bearertokens);
        this.integers = Collections.unmodifiableMap(integers);
        this.safelongs = Collections.unmodifiableMap(safelongs);
        this.datetimes = Collections.unmodifiableMap(datetimes);
        this.uuids = Collections.unmodifiableMap(uuids);
    }

    @JsonProperty("strings")
    public Map<StringAliasExample, ManyFieldExample> getStrings() {
        return this.strings;
    }

    @JsonProperty("rids")
    public Map<RidAliasExample, ManyFieldExample> getRids() {
        return this.rids;
    }

    @JsonProperty("bearertokens")
    public Map<BearerTokenAliasExample, ManyFieldExample> getBearertokens() {
        return this.bearertokens;
    }

    @JsonProperty("integers")
    public Map<IntegerAliasExample, ManyFieldExample> getIntegers() {
        return this.integers;
    }

    @JsonProperty("safelongs")
    public Map<SafeLongAliasExample, ManyFieldExample> getSafelongs() {
        return this.safelongs;
    }

    @JsonProperty("datetimes")
    public Map<DateTimeAliasExample, ManyFieldExample> getDatetimes() {
        return this.datetimes;
    }

    @JsonProperty("uuids")
    public Map<UuidAliasExample, ManyFieldExample> getUuids() {
        return this.uuids;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || (other instanceof AliasAsMapKeyExample && equalTo((AliasAsMapKeyExample) other));
    }

    private boolean equalTo(AliasAsMapKeyExample other) {
        return this.strings.equals(other.strings)
                && this.rids.equals(other.rids)
                && this.bearertokens.equals(other.bearertokens)
                && this.integers.equals(other.integers)
                && this.safelongs.equals(other.safelongs)
                && this.datetimes.equals(other.datetimes)
                && this.uuids.equals(other.uuids);
    }

    @Override
    public int hashCode() {
        if (memoizedHashCode == 0) {
            memoizedHashCode =
                    Objects.hash(
                            strings, rids, bearertokens, integers, safelongs, datetimes, uuids);
        }
        return memoizedHashCode;
    }

    @Override
    public String toString() {
        return new StringBuilder("AliasAsMapKeyExample")
                .append("{")
                .append("strings")
                .append(": ")
                .append(strings)
                .append(", ")
                .append("rids")
                .append(": ")
                .append(rids)
                .append(", ")
                .append("bearertokens")
                .append(": ")
                .append(bearertokens)
                .append(", ")
                .append("integers")
                .append(": ")
                .append(integers)
                .append(", ")
                .append("safelongs")
                .append(": ")
                .append(safelongs)
                .append(", ")
                .append("datetimes")
                .append(": ")
                .append(datetimes)
                .append(", ")
                .append("uuids")
                .append(": ")
                .append(uuids)
                .append("}")
                .toString();
    }

    private static void validateFields(
            Map<StringAliasExample, ManyFieldExample> strings,
            Map<RidAliasExample, ManyFieldExample> rids,
            Map<BearerTokenAliasExample, ManyFieldExample> bearertokens,
            Map<IntegerAliasExample, ManyFieldExample> integers,
            Map<SafeLongAliasExample, ManyFieldExample> safelongs,
            Map<DateTimeAliasExample, ManyFieldExample> datetimes,
            Map<UuidAliasExample, ManyFieldExample> uuids) {
        List<String> missingFields = null;
        missingFields = addFieldIfMissing(missingFields, strings, "strings");
        missingFields = addFieldIfMissing(missingFields, rids, "rids");
        missingFields = addFieldIfMissing(missingFields, bearertokens, "bearertokens");
        missingFields = addFieldIfMissing(missingFields, integers, "integers");
        missingFields = addFieldIfMissing(missingFields, safelongs, "safelongs");
        missingFields = addFieldIfMissing(missingFields, datetimes, "datetimes");
        missingFields = addFieldIfMissing(missingFields, uuids, "uuids");
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
                missingFields = new ArrayList<>(7);
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
        private Map<StringAliasExample, ManyFieldExample> strings = new LinkedHashMap<>();

        private Map<RidAliasExample, ManyFieldExample> rids = new LinkedHashMap<>();

        private Map<BearerTokenAliasExample, ManyFieldExample> bearertokens = new LinkedHashMap<>();

        private Map<IntegerAliasExample, ManyFieldExample> integers = new LinkedHashMap<>();

        private Map<SafeLongAliasExample, ManyFieldExample> safelongs = new LinkedHashMap<>();

        private Map<DateTimeAliasExample, ManyFieldExample> datetimes = new LinkedHashMap<>();

        private Map<UuidAliasExample, ManyFieldExample> uuids = new LinkedHashMap<>();

        private Builder() {}

        public Builder from(AliasAsMapKeyExample other) {
            strings(other.getStrings());
            rids(other.getRids());
            bearertokens(other.getBearertokens());
            integers(other.getIntegers());
            safelongs(other.getSafelongs());
            datetimes(other.getDatetimes());
            uuids(other.getUuids());
            return this;
        }

        @JsonSetter("strings")
        public Builder strings(Map<StringAliasExample, ManyFieldExample> strings) {
            this.strings.clear();
            this.strings.putAll(Objects.requireNonNull(strings, "strings cannot be null"));
            return this;
        }

        public Builder putAllStrings(Map<StringAliasExample, ManyFieldExample> strings) {
            this.strings.putAll(Objects.requireNonNull(strings, "strings cannot be null"));
            return this;
        }

        public Builder strings(StringAliasExample key, ManyFieldExample value) {
            this.strings.put(key, value);
            return this;
        }

        @JsonSetter("rids")
        public Builder rids(Map<RidAliasExample, ManyFieldExample> rids) {
            this.rids.clear();
            this.rids.putAll(Objects.requireNonNull(rids, "rids cannot be null"));
            return this;
        }

        public Builder putAllRids(Map<RidAliasExample, ManyFieldExample> rids) {
            this.rids.putAll(Objects.requireNonNull(rids, "rids cannot be null"));
            return this;
        }

        public Builder rids(RidAliasExample key, ManyFieldExample value) {
            this.rids.put(key, value);
            return this;
        }

        @JsonSetter("bearertokens")
        public Builder bearertokens(Map<BearerTokenAliasExample, ManyFieldExample> bearertokens) {
            this.bearertokens.clear();
            this.bearertokens.putAll(
                    Objects.requireNonNull(bearertokens, "bearertokens cannot be null"));
            return this;
        }

        public Builder putAllBearertokens(
                Map<BearerTokenAliasExample, ManyFieldExample> bearertokens) {
            this.bearertokens.putAll(
                    Objects.requireNonNull(bearertokens, "bearertokens cannot be null"));
            return this;
        }

        public Builder bearertokens(BearerTokenAliasExample key, ManyFieldExample value) {
            this.bearertokens.put(key, value);
            return this;
        }

        @JsonSetter("integers")
        public Builder integers(Map<IntegerAliasExample, ManyFieldExample> integers) {
            this.integers.clear();
            this.integers.putAll(Objects.requireNonNull(integers, "integers cannot be null"));
            return this;
        }

        public Builder putAllIntegers(Map<IntegerAliasExample, ManyFieldExample> integers) {
            this.integers.putAll(Objects.requireNonNull(integers, "integers cannot be null"));
            return this;
        }

        public Builder integers(IntegerAliasExample key, ManyFieldExample value) {
            this.integers.put(key, value);
            return this;
        }

        @JsonSetter("safelongs")
        public Builder safelongs(Map<SafeLongAliasExample, ManyFieldExample> safelongs) {
            this.safelongs.clear();
            this.safelongs.putAll(Objects.requireNonNull(safelongs, "safelongs cannot be null"));
            return this;
        }

        public Builder putAllSafelongs(Map<SafeLongAliasExample, ManyFieldExample> safelongs) {
            this.safelongs.putAll(Objects.requireNonNull(safelongs, "safelongs cannot be null"));
            return this;
        }

        public Builder safelongs(SafeLongAliasExample key, ManyFieldExample value) {
            this.safelongs.put(key, value);
            return this;
        }

        @JsonSetter("datetimes")
        public Builder datetimes(Map<DateTimeAliasExample, ManyFieldExample> datetimes) {
            this.datetimes.clear();
            this.datetimes.putAll(Objects.requireNonNull(datetimes, "datetimes cannot be null"));
            return this;
        }

        public Builder putAllDatetimes(Map<DateTimeAliasExample, ManyFieldExample> datetimes) {
            this.datetimes.putAll(Objects.requireNonNull(datetimes, "datetimes cannot be null"));
            return this;
        }

        public Builder datetimes(DateTimeAliasExample key, ManyFieldExample value) {
            this.datetimes.put(key, value);
            return this;
        }

        @JsonSetter("uuids")
        public Builder uuids(Map<UuidAliasExample, ManyFieldExample> uuids) {
            this.uuids.clear();
            this.uuids.putAll(Objects.requireNonNull(uuids, "uuids cannot be null"));
            return this;
        }

        public Builder putAllUuids(Map<UuidAliasExample, ManyFieldExample> uuids) {
            this.uuids.putAll(Objects.requireNonNull(uuids, "uuids cannot be null"));
            return this;
        }

        public Builder uuids(UuidAliasExample key, ManyFieldExample value) {
            this.uuids.put(key, value);
            return this;
        }

        public AliasAsMapKeyExample build() {
            return new AliasAsMapKeyExample(
                    strings, rids, bearertokens, integers, safelongs, datetimes, uuids);
        }
    }
}
