/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.testing;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.palantir.remoting3.ext.jackson.ObjectMappers;
import com.palantir.ri.ResourceIdentifier;
import com.palantir.tokens.auth.BearerToken;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.time.OffsetDateTime;
import java.util.UUID;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class SerializationServlet extends HttpServlet {

    private static final ObjectMapper mapper = ObjectMappers.newServerObjectMapper();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String method = req.getPathInfo().substring(1);
        final Class<?> clazz;
        switch (method) {
            case "string":
                clazz = String.class;
                break;
            case "integer":
                clazz = Integer.class;
                break;
            case "double":
                clazz = Double.class;
                break;
            case "boolean":
                clazz = Boolean.class;
                break;
            case "safelong":
                clazz = Long.class;
                break;
            case "rid":
                clazz = ResourceIdentifier.class;
                break;
            case "bearertoken":
                clazz = BearerToken.class;
                break;
            case "uuid":
                clazz = UUID.class;
                break;
            case "datetime":
                clazz = OffsetDateTime.class;
                break;
            case "binary":
                clazz = String.class;
                break;
            default:
                resp.setStatus(500);
                return;
        }

        serialize(clazz, req.getReader(), resp.getWriter());
    }

    //
    //    public Response integer(int value) {
    //        return serialize(value);
    //    }
    //
    //    public Response _double(double value) {
    //        return serialize(value);
    //    }
    //
    //    public Response _boolean(boolean value) {
    //        return serialize(value);
    //    }
    //
    //    public Response safelong(SafeLong value) {
    //        return serialize(value);
    //    }
    //
    //    public Response rid(ResourceIdentifier value) {
    //        return serialize(value);
    //    }
    //
    //    public Response bearertoken(BearerToken value) {
    //        return serialize(value);
    //    }
    //
    //    public Response uuid(UUID value) {
    //        return serialize(value);
    //    }
    //
    //    public Response datetime(ZonedDateTime value) {
    //        return serialize(value);
    //    }
    //
    //    public Response binary(InputStream value) {
    //        return serialize(value);
    //    }

    private static <T> void serialize(Class<T> type, Reader input, Writer output) {
        try {
            T value = mapper.readValue(input, type);
            mapper.writeValue(output, value);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to deserialize or serialize", e);
        }
    }
}
