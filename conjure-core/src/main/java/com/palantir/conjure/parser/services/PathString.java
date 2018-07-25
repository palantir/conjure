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

package com.palantir.conjure.parser.services;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.base.Preconditions;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.util.syntacticpath.Path;
import com.palantir.util.syntacticpath.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.glassfish.jersey.uri.UriTemplate;
import org.glassfish.jersey.uri.internal.UriTemplateParser;
import org.immutables.value.Value;

/** Represents a HTTP path in a {@link ServiceDefinition conjure service definition}. */
@Value.Immutable
@ConjureImmutablesStyle
public abstract class PathString {

    /** Returns the well-formed path associated with this path definition. */
    public abstract Path path();

    private static final Pattern SEGMENT_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_-]*$");
    private static final Pattern PARAM_SEGMENT_PATTERN = Pattern.compile("^\\{" + ParameterName.PATTERN + "}$");
    private static final Pattern PARAM_REGEX_SEGMENT_PATTERN =
            Pattern.compile(
                    "^\\{" + ParameterName.PATTERN + "(" + Pattern.quote(":.+") + "|" + Pattern.quote(":.*") + ")"
                            + "}$");

    /** Creates a new instance if the syntax is correct. */
    @Value.Check
    protected final void check() {
        Preconditions.checkArgument(path().isAbsolute(),
                "Conjure paths must be absolute, i.e., start with '/': %s", path());
        Preconditions.checkArgument(path().getSegments().isEmpty() || !path().isFolder(),
                "Conjure paths must not end with a '/': %s", path());

        for (String segment : path().getSegments()) {
            Preconditions.checkArgument(
                    SEGMENT_PATTERN.matcher(segment).matches()
                            || PARAM_SEGMENT_PATTERN.matcher(segment).matches()
                            || PARAM_REGEX_SEGMENT_PATTERN.matcher(segment).matches(),
                    "Segment %s of path %s did not match required segment patterns %s or parameter name "
                            + "patterns %s or %s",
                    segment, path(), SEGMENT_PATTERN, PARAM_SEGMENT_PATTERN, PARAM_REGEX_SEGMENT_PATTERN);
        }

        // verify that path template variables are unique
        Set<String> templateVars = new HashSet<>();
        new UriTemplate(path().toString()).getTemplateVariables().stream().forEach(var -> {
            Preconditions.checkState(!templateVars.contains(var),
                    "Path parameter %s appears more than once in path %s", var, path());
            templateVars.add(var);
        });

        UriTemplateParser uriTemplateParser = new UriTemplateParser(path().toString());
        Map<String, Pattern> nameToPattern = uriTemplateParser.getNameToPattern();
        String[] segments = uriTemplateParser.getNormalizedTemplate().split("/");
        for (int i = 0; i < segments.length; i++) {
            if (!(segments[i].startsWith("{") && segments[i].endsWith("}"))) {
                // path literal
                continue;
            }

            // variable
            Pattern varPattern = nameToPattern.get(segments[i].substring(1, segments[i].length() - 1));
            if (varPattern.equals(UriTemplateParser.TEMPLATE_VALUE_PATTERN)) {
                // no regular expression specified -- OK
                continue;
            }

            // if regular expression was specified, it must be ".+" or ".*" based on invariant previously enforced
            Preconditions.checkState(i == segments.length - 1 || !varPattern.pattern().equals(".*"),
                    "Path parameter %s in path %s specifies regular expression %s, but this regular "
                            + "expression is only permitted if the path parameter is the last segment", segments[i],
                    path(), varPattern);
        }
    }

    /**
     * Returns this path "concatenated" with the given other path. For example, {@code "/abc".resolve("/def")} is the
     * path {@code /abc/def}.
     */
    public PathString resolve(
            PathString other) {
        final Path newPath;
        if (other.path().equals(Path.ROOT_PATH)) {
            // special-case since Path#relativize() only works on proper prefixes
            newPath = path();
        } else {
            newPath = path().resolve(Path.ROOT_PATH.relativize(other.path()));
        }
        return ImmutablePathString.builder().path(newPath).build();
    }

    /** Creates a new {@link PathString} from the given string, or throws an exception if it fails to validate. */
    @JsonCreator
    public static PathString of(String path) {
        Path parsed = Paths.get(path);
        return ImmutablePathString.builder().path(parsed).build();
    }

    @Override
    public final String toString() {
        return path().toString();
    }
}
