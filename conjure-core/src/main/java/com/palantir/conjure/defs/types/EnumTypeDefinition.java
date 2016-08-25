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

    Set<EnumValueDefinition> values();

    // we keep this check instead of pushing it down to EnumValueDefinition for better errors
    @Value.Check
    default void validate() {
        checkArgument(!Iterables.any(values(), s -> s.value().equalsIgnoreCase("UNKNOWN")),
                "UNKNOWN is a reserved enumeration value");

        Collection<EnumValueDefinition> invalidValues = Collections2.filter(values(),
                s -> !REQUIRED_FORMAT.matcher(s.value()).matches());
        checkArgument(invalidValues.isEmpty(),
                "Enumeration values must have format %s, illegal values: %s",
                REQUIRED_FORMAT,
                Iterables.transform(invalidValues, s -> s.value()));
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableEnumTypeDefinition.Builder {}

}
