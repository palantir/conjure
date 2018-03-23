package test;

import com.palantir.tokens.auth.AuthHeader;
import java.lang.String;
import javax.annotation.Generated;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

/** TestServiceDocs */
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/")
@Generated("com.palantir.conjure.gen.java.services.JerseyServiceGenerator")
public interface TestService {
    /** stringDocs */
    @GET
    @Path("base/string")
    String string(@HeaderParam("Authorization") AuthHeader authHeader);

    @GET
    @Path("base/stringEcho")
    String stringEcho(
            @HeaderParam("Authorization") AuthHeader authHeader,
            @HeaderParam("Header-String") String string);

    @GET
    @Path("base/integer")
    int integer(@HeaderParam("Authorization") AuthHeader authHeader);

    @GET
    @Path("base/integerEcho/{integer}")
    int integerEcho(
            @HeaderParam("Authorization") AuthHeader authHeader, @PathParam("integer") int integer);

    @GET
    @Path("base/queryEcho")
    String queryEcho(
            @HeaderParam("Authorization") AuthHeader authHeader,
            @QueryParam("queryParam") int integer);

    @GET
    @Path("base/complex")
    Complex complex(@HeaderParam("Authorization") AuthHeader authHeader);

    @POST
    @Path("base/complexEcho")
    Complex complexEcho(@HeaderParam("Authorization") AuthHeader authHeader, Complex complex);

    @POST
    @Path("base/binaryEcho")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    StreamingOutput binaryEcho(@HeaderParam("Authorization") AuthHeader authHeader, String string);
}
