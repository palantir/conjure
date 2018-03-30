/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.testing;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.palantir.conjure.testing.reference.MyEnum;
import com.palantir.conjure.testing.reference.MyObject;
import com.palantir.conjure.testing.reference.Union;
import com.palantir.remoting3.ext.jackson.ObjectMappers;
import com.palantir.ri.ResourceIdentifier;
import com.palantir.tokens.auth.BearerToken;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class SerializationServlet extends HttpServlet {

    private static final ObjectMapper mapper = ObjectMappers.newServerObjectMapper();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String method = req.getPathInfo().substring(1);
        final TypeReference<?> clazz;
        switch (method) {
            // Primitives
            case "string":
                clazz = new TypeReference<String>() {};
                break;
            case "integer":
                clazz = new TypeReference<Integer>() {};
                break;
            case "double":
                clazz = new TypeReference<Double>() {};
                break;
            case "boolean":
                clazz = new TypeReference<Boolean>() {};
                break;
            case "safelong":
                clazz = new TypeReference<Long>() {};
                break;
            case "rid":
                clazz = new TypeReference<ResourceIdentifier>() {};
                break;
            case "bearertoken":
                clazz = new TypeReference<BearerToken>() {};
                break;
            case "uuid":
                clazz = new TypeReference<UUID>() {};
                break;
            case "datetime":
                clazz = new TypeReference<OffsetDateTime>() {};
                break;
            case "binary":
                clazz = new TypeReference<String>() {};
                break;

            // Built-ins
            case "map":
                clazz = new TypeReference<Map<String, Object>>() {};
                break;
            case "list":
                clazz = new TypeReference<List<Object>>() {};
                break;
            case "set":
                clazz = new TypeReference<Set<Object>>() {};
                break;
            case "optional":
                clazz = new TypeReference<Optional<Object>>() {};
                break;

            // Complex
            case "object":
                clazz = new TypeReference<MyObject>() {};
                break;
            case "union":
                clazz = new TypeReference<Union>() {};
                break;
            case "enum":
                clazz = new TypeReference<MyEnum>() {};
                break;

            default:
                resp.setStatus(500);
                return;
        }

        serialize(clazz, req.getReader(), resp.getWriter());
    }

    private static void serialize(TypeReference<?> type, Reader input, Writer output) {
        try {
            Object value = mapper.readValue(input, type);
            mapper.writeValue(output, value);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to deserialize or serialize", e);
        }
    }
}
