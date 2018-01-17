/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.services;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.base.Preconditions;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.ConjureMetrics;
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
public abstract class PathDefinition {

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
        ConjureMetrics.histogram(templateVars.size(), PathDefinition.class, "template-vars");

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
    public PathDefinition resolve(PathDefinition other) {
        final Path newPath;
        if (other.path().equals(Path.ROOT_PATH)) {
            // special-case since Path#relativize() only works on proper prefixes
            newPath = path();
        } else {
            newPath = path().resolve(Path.ROOT_PATH.relativize(other.path()));
        }
        return ImmutablePathDefinition.builder().path(newPath).build();
    }

    /** Creates a new {@link PathDefinition} from the given string, or throws an exception if it fails to validate. */
    @JsonCreator
    public static PathDefinition of(String path) {
        Path parsed = Paths.get(path);
        return ImmutablePathDefinition.builder().path(parsed).build();
    }

    @Override
    public final String toString() {
        return path().toString();
    }
}
