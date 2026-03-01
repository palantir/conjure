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

package com.palantir.conjure.defs.validator;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.palantir.conjure.spec.HttpPath;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

public final class HttpPathValidatorTest {

    @Test
    public void testPathsMustBeAbsolute() {
        assertThatThrownBy(() -> validate("abc"))
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
    public void testPathSegmentsMustObeySyntax() {
        for (PathSegmentTestCase currCase : new PathSegmentTestCase[] {
            new PathSegmentTestCase("/123", "123"),
            new PathSegmentTestCase("/abc/$%^", "$%^"),
            new PathSegmentTestCase("/abc/{123}", "{123}"),
        }) {
            assertThatThrownBy(() -> validate(currCase.path))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(String.format(
                            "Segment %s of path %s did not match required segment patterns "
                                    + "^[a-zA-Z][a-zA-Z0-9._-]*$ or parameter name patterns "
                                    + "^\\{[a-z][a-z0-9]*([A-Z0-9][a-z0-9]+)*}$ or "
                                    + "^\\{[a-z][a-z0-9]*([A-Z0-9][a-z0-9]+)*"
                                    + "(" + Pattern.quote(":.+") + "|" + Pattern.quote(":.*") + ")"
                                    + "}$",
                            currCase.invalidSegment,
                            currCase.path));
        }
    }

    @Test
    public void testNonEmptyPathsMustNotEndWithSlash() {
        assertThatThrownBy(() -> validate("/abc/"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Conjure paths must not end with a '/': /abc/");
    }

    @Test
    public void testValidPaths() {
        validate("/");
        validate("/a/b/c");
        validate("/abc");
        validate("/{foo}");
        validate("/abc/{foo}/bar");
        validate("/abc/{foo:.+}");
        validate("/abc/v1.2/{foo}");
    }

    private static void validate(String path) {
        HttpPathValidator.validate(HttpPath.of(path));
    }
}
