/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.services;

import com.google.common.base.Preconditions;
import com.palantir.conjure.parser.ConjureMetrics;
import com.palantir.conjure.spec.ArgumentName;
import com.palantir.conjure.spec.HttpPath;
import com.palantir.util.syntacticpath.Path;
import com.palantir.util.syntacticpath.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.glassfish.jersey.uri.UriTemplate;
import org.glassfish.jersey.uri.internal.UriTemplateParser;

public final class HttpPathWrapper {

    private HttpPathWrapper() {}

    public static final String PATTERN = "[a-z][a-z0-9]*([A-Z0-9][a-z0-9]+)*";
    private static final Pattern SEGMENT_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_-]*$");
    private static final Pattern PARAM_SEGMENT_PATTERN = Pattern.compile("^\\{" + PATTERN + "}$");
    private static final Pattern PARAM_REGEX_SEGMENT_PATTERN =
            Pattern.compile(
                    "^\\{" + PATTERN + "(" + Pattern.quote(":.+") + "|" + Pattern.quote(":.*") + ")"
                            + "}$");

    /**
     * returns path arguments of the http path.
     */
    public static Set<ArgumentName> pathArgs(String httpPath) {
        validate(httpPath);
        UriTemplate uriTemplate = new UriTemplate(httpPath);
        return uriTemplate.getTemplateVariables()
                .stream()
                .map(ArgumentName::of)
                .collect(Collectors.toSet());
    }

    /** validates if a new instance has the correct syntax. */
    public static void validate(String httpPath) {
        Path path = Paths.get(httpPath);
        Preconditions.checkArgument(path.isAbsolute(),
                "Conjure paths must be absolute, i.e., start with '/': %s", path);
        Preconditions.checkArgument(path.getSegments().isEmpty() || !path.isFolder(),
                "Conjure paths must not end with a '/': %s", path);

        for (String segment : path.getSegments()) {
            Preconditions.checkArgument(
                    SEGMENT_PATTERN.matcher(segment).matches()
                            || PARAM_SEGMENT_PATTERN.matcher(segment).matches()
                            || PARAM_REGEX_SEGMENT_PATTERN.matcher(segment).matches(),
                    "Segment %s of path %s did not match required segment patterns %s or parameter name "
                            + "patterns %s or %s",
                    segment, path, SEGMENT_PATTERN, PARAM_SEGMENT_PATTERN, PARAM_REGEX_SEGMENT_PATTERN);
        }

        // verify that path template variables are unique
        Set<String> templateVars = new HashSet<>();
        new UriTemplate(path.toString()).getTemplateVariables().stream().forEach(var -> {
            Preconditions.checkState(!templateVars.contains(var),
                    "Path parameter %s appears more than once in path %s", var, path);
            templateVars.add(var);
        });
        ConjureMetrics.histogram(templateVars.size(), HttpPathWrapper.class, "template-vars");

        UriTemplateParser uriTemplateParser = new UriTemplateParser(path.toString());
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
                    path, varPattern);
        }
    }

    public static HttpPath httpPath(String httpPath) {
        validate(httpPath);
        return HttpPath.of(httpPath);
    }

    public static String withoutLeadingSlash(String httpPath) {
        if (httpPath.startsWith("/")) {
            return httpPath.substring(1);
        } else {
            return httpPath;
        }
    }
}
