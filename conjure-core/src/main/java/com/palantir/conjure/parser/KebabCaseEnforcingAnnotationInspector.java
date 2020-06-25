/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.palantir.conjure.parser;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.util.VersionUtil;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.google.common.base.Preconditions;
import java.util.regex.Pattern;

/**
 * An {@link AnnotationIntrospector} that is no-op except for the
 * {@link AnnotationIntrospector#findNameForDeserialization(Annotated)}
 * method: verifies that any {@link JsonProperty} specified on the deserialization target stipulates {@code kebab-case}
 * JSON field names.
 */
public final class KebabCaseEnforcingAnnotationInspector extends AnnotationIntrospector {

    private static final Pattern KEBAB_CASE_PATTERN = Pattern.compile("[a-z]+(-[a-z]+)*");

    @Override
    public Version version() {
        return VersionUtil.parseVersion("0.0.1", "foo", "bar");
    }

    @Override
    public PropertyName findNameForDeserialization(Annotated annotatedEntity) {
        // This logic relies on the fact that Immutables generates setters annotated with @JsonProperty.
        // It thus becomes obsolete whenever we move away from Immutables and the deserialization target no longer
        // carries those annotations.

        JsonProperty propertyAnnotation = _findAnnotation(annotatedEntity, JsonProperty.class);
        if (propertyAnnotation != null) {
            String jsonFieldName = propertyAnnotation.value();
            Preconditions.checkArgument(
                    KEBAB_CASE_PATTERN.matcher(jsonFieldName).matches(),
                    "Conjure grammar requires kebab-case field names: %s",
                    jsonFieldName);
        }

        if (annotatedEntity instanceof AnnotatedMethod) {
            AnnotatedMethod maybeSetter = (AnnotatedMethod) annotatedEntity;
            if (maybeSetter.getName().startsWith("set")) {
                // As a pre-caution, require that all setters have a JsonProperty annotation.
                Preconditions.checkArgument(
                        _findAnnotation(annotatedEntity, JsonProperty.class) != null,
                        "All setter ({@code set*}) deserialization targets require @JsonProperty annotations: %s",
                        maybeSetter.getName());
            }
        }

        return null; // delegate to the next introspector in an AnnotationIntrospectorPair.
    }
}
