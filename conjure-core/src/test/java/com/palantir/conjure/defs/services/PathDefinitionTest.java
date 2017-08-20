/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.regex.Pattern;
import org.junit.Test;

public final class PathDefinitionTest {

    @Test
    public void testPathsMustBeAbsolute() throws Exception {
        assertThatThrownBy(() -> path("abc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Conjure paths must be absolute, i.e., start with '/': abc");
    }

    private static final class PathSegmentTestCase {
        private final String path;
        private final String invalidSegment;

        private PathSegmentTestCase(String path, String invalidSegment) {
            this.path = path;
            this.invalidSegment = invalidSegment;
        }
    }

    @Test
    public void testPathSegmentsMustObeySyntax() throws Exception {
        for (PathSegmentTestCase currCase : new PathSegmentTestCase[] {
                new PathSegmentTestCase("/123", "123"),
                new PathSegmentTestCase("/abc/$%^", "$%^"),
                new PathSegmentTestCase("/abc/{123}", "{123}"),
                }) {
            assertThatThrownBy(() -> path(currCase.path))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(String.format("Segment %s of path %s did not match required segment patterns "
                                    + "^[a-zA-Z][a-zA-Z0-9_-]*$ or parameter name patterns "
                                    + "^\\{[a-z][a-z0-9]*([A-Z0-9][a-z0-9]+)*}$ or "
                                    + "^\\{[a-z][a-z0-9]*([A-Z0-9][a-z0-9]+)*"
                                    + "(" + Pattern.quote(":.+") + "|" + Pattern.quote(":.*") + ")"
                                    + "}$",
                            currCase.invalidSegment, currCase.path));
        }
    }

    @Test
    public void testNonEmptyPathsMustNotEndWithSlash() throws Exception {
        assertThatThrownBy(() -> path("/abc/"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Conjure paths must not end with a '/': /abc/");
    }

    @Test
    public void testValidPaths() throws Exception {
        path("/");
        path("/a/b/c");
        path("/abc");
        path("/{foo}");
        path("/abc/{foo}/bar");
        path("/abc/{foo:.+}");
    }

    @Test
    public void testResolvePaths() throws Exception {
        assertThat(path("/").resolve(path("/"))).isEqualTo(path("/"));
        assertThat(path("/abc").resolve(path("/"))).isEqualTo(path("/abc"));
        assertThat(path("/abc").resolve(path("/def"))).isEqualTo(path("/abc/def"));
        assertThat(path("/").resolve(path("/def"))).isEqualTo(path("/def"));
        assertThat(path("/a/b/c").resolve(path("/d/e/f"))).isEqualTo(path("/a/b/c/d/e/f"));
    }

    private static PathDefinition path(String path) {
        return PathDefinition.of(path);
    }
}
