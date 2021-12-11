/*
 * (c) Copyright 2021 Palantir Technologies Inc. All rights reserved.
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

package com.palantir.conjure;

public enum PackagePattern implements SimplifiedPattern {
    INSTANCE;

    public static PackagePattern get() {
        return INSTANCE;
    }

    @Override
    public String pattern() {
        return "^([a-z][a-z0-9]+(\\.[a-z][a-z0-9]*)*)?$";
    }

    @Override
    @SuppressWarnings({"checkstyle:CyclomaticComplexity", "checkstyle:ModifiedControlVariable"})
    public boolean matches(String value) {
        int len = value.length();
        // Must be either empty, or start with at least two chars
        if (len == 0) {
            return true;
        }
        if (len == 1) {
            return false;
        }

        char firstChar = value.charAt(0);
        if (firstChar < 'a' || firstChar > 'z') {
            return false;
        }

        for (int i = 1; i < len; i++) {
            char curChar = value.charAt(i);

            if (curChar == '.') {
                if (i == 1) {
                    // Need to have two non-. chars at start
                    return false;
                }
                i++;
                if (i >= len) {
                    return false;
                }
                curChar = value.charAt(i);

                if (curChar < 'a' || curChar > 'z') {
                    return false;
                }
            } else {
                boolean isLowerOrNumeric = (curChar >= 'a' && curChar <= 'z') || (curChar >= '0' && curChar <= '9');

                if (!isLowerOrNumeric) {
                    return false;
                }
            }
        }

        return true;
    }
}
