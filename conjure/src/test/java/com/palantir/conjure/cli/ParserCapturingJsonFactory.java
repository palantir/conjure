/*
 * (c) Copyright 2022 Palantir Technologies Inc. All rights reserved.
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

package com.palantir.conjure.cli;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import java.io.DataInput;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A JsonFactory that captures the parser when it's created, making it available to use elsewhere.
 */
public final class ParserCapturingJsonFactory extends JsonFactory {

    private final JsonFactory delegate;
    private final AtomicReference<JsonParser> parserReference = new AtomicReference<>();

    public ParserCapturingJsonFactory(JsonFactory delegate) {
        this.delegate = delegate;
    }

    public JsonParser getParser() {
        return Optional.ofNullable(parserReference.get()).orElseThrow();
    }

    @Override
    public JsonParser createParser(File file) throws IOException, JsonParseException {
        return captureParser(delegate.createParser(file));
    }

    @SuppressWarnings("deprecation")
    @Override
    public JsonParser createParser(URL url) throws IOException, JsonParseException {
        // Deprecated, but just calling the same method on the delegate as we're overriding
        return captureParser(delegate.createParser(url));
    }

    @Override
    public JsonParser createParser(InputStream in) throws IOException, JsonParseException {
        return captureParser(delegate.createParser(in));
    }

    @Override
    public JsonParser createParser(Reader reader) throws IOException, JsonParseException {
        return captureParser(delegate.createParser(reader));
    }

    @Override
    public JsonParser createParser(byte[] data) throws IOException, JsonParseException {
        return captureParser(delegate.createParser(data));
    }

    @Override
    public JsonParser createParser(byte[] data, int offset, int len) throws IOException, JsonParseException {
        return captureParser(delegate.createParser(data, offset, len));
    }

    @Override
    public JsonParser createParser(String content) throws IOException, JsonParseException {
        return captureParser(delegate.createParser(content));
    }

    @Override
    public JsonParser createParser(char[] content) throws IOException {
        return captureParser(delegate.createParser(content));
    }

    @Override
    public JsonParser createParser(char[] content, int offset, int len) throws IOException {
        return captureParser(delegate.createParser(content, offset, len));
    }

    @Override
    public JsonParser createParser(DataInput in) throws IOException {
        return captureParser(delegate.createParser(in));
    }

    private JsonParser captureParser(JsonParser parser) {
        this.parserReference.set(parser);
        return parser;
    }
}
