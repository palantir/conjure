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

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BinaryNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.fasterxml.jackson.databind.util.RawValue;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * A JsonNode factory that records the location of each node during parsing.
 */
public final class LocationTrackingNodeFactory extends JsonNodeFactory {
    private final JsonNodeFactory delegate;
    private final Supplier<JsonParser> parserSupplier;

    // The JsonNode's hashCode is calculated based on its children. During parsing the children haven't
    // yet been populated, so instead we use an identity hash map .
    private final Map<JsonNode, JsonLocation> nodeLocations = new IdentityHashMap<>();

    public LocationTrackingNodeFactory(JsonNodeFactory delegate, Supplier<JsonParser> parserSupplier) {
        this.delegate = delegate;
        this.parserSupplier = parserSupplier;
    }

    /**
     * Given a node, find its location.
     */
    public JsonLocation getLocationForNode(JsonNode jsonNode) {
        return Optional.ofNullable(nodeLocations.get(jsonNode)).orElseThrow();
    }

    @Override
    public BooleanNode booleanNode(boolean value) {
        return recordLocation(delegate.booleanNode(value));
    }

    @Override
    public NullNode nullNode() {
        return recordLocation(delegate.nullNode());
    }

    @Override
    public NumericNode numberNode(byte value) {
        return recordLocation(delegate.numberNode(value));
    }

    @Override
    public ValueNode numberNode(Byte value) {
        return recordLocation(delegate.numberNode(value));
    }

    @Override
    public NumericNode numberNode(short value) {
        return recordLocation(delegate.numberNode(value));
    }

    @Override
    public ValueNode numberNode(Short value) {
        return recordLocation(delegate.numberNode(value));
    }

    @Override
    public NumericNode numberNode(int value) {
        return recordLocation(delegate.numberNode(value));
    }

    @Override
    public ValueNode numberNode(Integer value) {
        return recordLocation(delegate.numberNode(value));
    }

    @Override
    public NumericNode numberNode(long value) {
        return recordLocation(delegate.numberNode(value));
    }

    @Override
    public ValueNode numberNode(Long value) {
        return recordLocation(delegate.numberNode(value));
    }

    @Override
    public ValueNode numberNode(BigInteger value) {
        return recordLocation(delegate.numberNode(value));
    }

    @Override
    public NumericNode numberNode(float value) {
        return recordLocation(delegate.numberNode(value));
    }

    @Override
    public ValueNode numberNode(Float value) {
        return recordLocation(delegate.numberNode(value));
    }

    @Override
    public NumericNode numberNode(double value) {
        return recordLocation(delegate.numberNode(value));
    }

    @Override
    public ValueNode numberNode(Double value) {
        return recordLocation(delegate.numberNode(value));
    }

    @Override
    public ValueNode numberNode(BigDecimal value) {
        return recordLocation(delegate.numberNode(value));
    }

    @Override
    public TextNode textNode(String text) {
        return recordLocation(delegate.textNode(text));
    }

    @Override
    public BinaryNode binaryNode(byte[] data) {
        return recordLocation(delegate.binaryNode(data));
    }

    @Override
    public BinaryNode binaryNode(byte[] data, int offset, int length) {
        return recordLocation(delegate.binaryNode(data, offset, length));
    }

    @Override
    public ValueNode pojoNode(Object pojo) {
        return recordLocation(delegate.pojoNode(pojo));
    }

    @Override
    public ValueNode rawValueNode(RawValue value) {
        return recordLocation(delegate.rawValueNode(value));
    }

    @Override
    public ArrayNode arrayNode() {
        return recordLocation(delegate.arrayNode());
    }

    @Override
    public ArrayNode arrayNode(int capacity) {
        return recordLocation(delegate.arrayNode(capacity));
    }

    @Override
    public ObjectNode objectNode() {
        return recordLocation(delegate.objectNode());
    }

    private <T extends JsonNode> T recordLocation(T node) {
        JsonParser parser = parserSupplier.get();

        JsonLocation loc = parser.currentTokenLocation();

        // The YAML parser has already moved on to the next line if the token is an end-of-struct
        if (parser instanceof YAMLParser && parser.currentToken().isStructEnd()) {
            loc = new JsonLocation(loc.contentReference(), loc.getCharOffset(), loc.getLineNr() - 1, loc.getColumnNr());
        }

        nodeLocations.put(node, loc);
        return node;
    }
}
