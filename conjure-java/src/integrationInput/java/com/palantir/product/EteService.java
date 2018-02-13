package com.palantir.product;

import com.palantir.conjure.lib.SafeLong;
import com.palantir.ri.ResourceIdentifier;
import com.palantir.tokens.auth.AuthHeader;
import com.palantir.tokens.auth.BearerToken;
import java.lang.String;
import java.time.ZonedDateTime;
import java.util.Optional;
import javax.annotation.Generated;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/")
@Generated("com.palantir.conjure.gen.java.services.JerseyServiceGenerator")
public interface EteService {
    @GET
    @Path("base/string")
    String string(@HeaderParam("Authorization") AuthHeader authHeader);

    @GET
    @Path("base/integer")
    int integer(@HeaderParam("Authorization") AuthHeader authHeader);

    @GET
    @Path("base/double")
    double double_(@HeaderParam("Authorization") AuthHeader authHeader);

    @GET
    @Path("base/boolean")
    boolean boolean_(@HeaderParam("Authorization") AuthHeader authHeader);

    @GET
    @Path("base/safelong")
    SafeLong safelong(@HeaderParam("Authorization") AuthHeader authHeader);

    @GET
    @Path("base/rid")
    ResourceIdentifier rid(@HeaderParam("Authorization") AuthHeader authHeader);

    @GET
    @Path("base/bearertoken")
    BearerToken bearertoken(@HeaderParam("Authorization") AuthHeader authHeader);

    @GET
    @Path("base/optionalString")
    Optional<String> optionalString(@HeaderParam("Authorization") AuthHeader authHeader);

    @GET
    @Path("base/optionalEmpty")
    Optional<String> optionalEmpty(@HeaderParam("Authorization") AuthHeader authHeader);

    @GET
    @Path("base/datetime")
    ZonedDateTime datetime(@HeaderParam("Authorization") AuthHeader authHeader);

    @GET
    @Path("base/binary")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    StreamingOutput binary(@HeaderParam("Authorization") AuthHeader authHeader);
}
