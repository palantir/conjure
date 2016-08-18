/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import java.util.Collection;
import java.util.Set;
import java.util.regex.Pattern;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@JsonDeserialize(as = ImmutableEnumTypeDefinition.class)
@Value.Immutable
public interface EnumTypeDefinition extends BaseObjectTypeDefinition {

    Pattern REQUIRED_FORMAT = Pattern.compile("[A-Z]+(_[A-Z]+)*");

    Set<String> values();

    @Value.Check
    default void validate() {
        checkArgument(!Iterables.any(values(), s -> s.equalsIgnoreCase("UNKNOWN")),
                "UNKNOWN is a reserved enumeration value");

        Collection<String> invalidValues = Collections2.filter(values(), s -> !REQUIRED_FORMAT.matcher(s).matches());
        checkArgument(invalidValues.isEmpty(),
                "Enumeration values must have format %s, illegal values: %s", REQUIRED_FORMAT, invalidValues);
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableEnumTypeDefinition.Builder {}

}
