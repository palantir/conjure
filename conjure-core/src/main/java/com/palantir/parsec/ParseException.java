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

public final class ParseException extends Exception {

    private static final long serialVersionUID = 8326653219913481816L;
    private final String message;
    private final ParserState state;

    public ParseException(String message, ParserState state) {
        this.message = message;
        this.state = state;
    }

    @Override
    public String getMessage() {
        // sb is the next 100 characters of the text being parsed
        StringBuilder sb = new StringBuilder();
        int curr;
        int counter = 0;
        curr = state.curr();
        while (-1 != curr && ++counter < 100) {
            sb.append((char) curr);
            curr = state.next();
        }

        String charInfo = "at or before character " + state.getCharPosition();
        String lineInfo = "on or before line " + state.getLine();

        return message + "\n" + charInfo + "\n" + lineInfo + "\n" + sb.toString();
    }

    public ParserState getState() {
        return state;
    }

}
