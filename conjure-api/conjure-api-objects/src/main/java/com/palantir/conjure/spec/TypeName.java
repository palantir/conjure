package com.palantir.conjure.spec;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Generated;

@JsonDeserialize(builder = TypeName.Builder.class)
@Generated("com.palantir.conjure.gen.java.types.BeanGenerator")
public final class TypeName {
    private final String name;

    private final String package_;

    private volatile int memoizedHashCode;

    private TypeName(String name, String package_) {
        validateFields(name, package_);
        this.name = name;
        this.package_ = package_;
    }

    /**
     * The name of the custom Conjure type or service. It must be in UpperCamelCase. Numbers are
     * permitted, but not at the beginning of a word. Allowed names: "FooBar", "XYCoordinate",
     * "Build2Request". Disallowed names: "fooBar", "2BuildRequest".
     */
    @JsonProperty("name")
    public String getName() {
        return this.name;
    }

    /**
     * A period-delimited string of package names. The package names must be lowercase. Numbers are
     * permitted, but not at the beginning of a package name. Allowed packages: "foo",
     * "com.palantir.bar", "com.palantir.foo.thing2". Disallowed packages: "Foo",
     * "com.palantir.foo.2thing".
     */
    @JsonProperty("package")
    public String getPackage() {
        return this.package_;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof TypeName && equalTo((TypeName) other));
    }

    private boolean equalTo(TypeName other) {
        return this.name.equals(other.name) && this.package_.equals(other.package_);
    }

    @Override
    public int hashCode() {
        if (memoizedHashCode == 0) {
            memoizedHashCode = Objects.hash(name, package_);
        }
        return memoizedHashCode;
    }

    @Override
    public String toString() {
        return new StringBuilder("TypeName")
                .append("{")
                .append("name")
                .append(": ")
                .append(name)
                .append(", ")
                .append("package")
                .append(": ")
                .append(package_)
                .append("}")
                .toString();
    }

    public static TypeName of(String name, String package_) {
        return builder().name(name).package_(package_).build();
    }

    private static void validateFields(String name, String package_) {
        List<String> missingFields = null;
        missingFields = addFieldIfMissing(missingFields, name, "name");
        missingFields = addFieldIfMissing(missingFields, package_, "package");
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
        private String name;

        private String package_;

        private Builder() {}

        public Builder from(TypeName other) {
            name(other.getName());
            package_(other.getPackage());
            return this;
        }

        /**
         * The name of the custom Conjure type or service. It must be in UpperCamelCase. Numbers are
         * permitted, but not at the beginning of a word. Allowed names: "FooBar", "XYCoordinate",
         * "Build2Request". Disallowed names: "fooBar", "2BuildRequest".
         */
        @JsonSetter("name")
        public Builder name(String name) {
            this.name = Objects.requireNonNull(name, "name cannot be null");
            return this;
        }

        /**
         * A period-delimited string of package names. The package names must be lowercase. Numbers
         * are permitted, but not at the beginning of a package name. Allowed packages: "foo",
         * "com.palantir.bar", "com.palantir.foo.thing2". Disallowed packages: "Foo",
         * "com.palantir.foo.2thing".
         */
        @JsonSetter("package")
        public Builder package_(String package_) {
            this.package_ = Objects.requireNonNull(package_, "package cannot be null");
            return this;
        }

        public TypeName build() {
            return new TypeName(name, package_);
        }
    }
}
