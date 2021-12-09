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

public final class KebabCasePattern implements SimplifiedPattern {
    @Override
    public String pattern() {
        return "^[a-z]((-[a-z]){1,2}[a-z0-9]|[a-z0-9])*(-[a-z])?$";
    }

    @Override
    @SuppressWarnings({"checkstyle:CyclomaticComplexity", "checkstyle:ModifiedControlVariable"})
    public boolean matches(String value) {
        int len = value.length();
        if (len == 0) {
            return false;
        }

        char firstChar = value.charAt(0);
        if (firstChar < 'a' || firstChar > 'z') {
            return false;
        }

        int dashChars = 0;
        for (int i = 1; i < len; i++) {
            char curChar = value.charAt(i);

            if (curChar == '-') {
                i++;
                if (i >= len) {
                    return false;
                }
                curChar = value.charAt(i);
                if (dashChars >= 2 || curChar < 'a' || curChar > 'z') {
                    return false;
                }
                dashChars++;
            } else {
                dashChars = 0;
                if (!((curChar >= 'a' && curChar <= 'z') || (curChar >= '0' && curChar <= '9'))) {
                    return false;
                }
            }
        }

        return dashChars <= 1;
    }
}
