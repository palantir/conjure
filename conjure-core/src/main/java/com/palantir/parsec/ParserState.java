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

package com.palantir.parsec;

import com.google.errorprone.annotations.CheckReturnValue;

public interface ParserState {

    /**
     * Current character in the stream.
     *
     * @return current character in the stream, of if unitialized, the first character
     */
    int curr();

    /**
     * Increments the character stream by one.
     *
     * @return current character in the stream
     */
    int next();

    /**
     * Mark beginning of an interesting feature (held as a stack).
     */
    @CheckReturnValue
    MarkedLocation mark();

    int getCharPosition();

    String fetchSnippetForException();

    /** Creates a new state object identical to this one, which does not follow location mutations to the original. */
    ParserState snapshot();

    interface MarkedLocation {
        /**
         * return to this marked position.
         */
        void rewind();
    }
}
