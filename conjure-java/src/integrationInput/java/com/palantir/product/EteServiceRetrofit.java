package com.palantir.product;

import com.palantir.conjure.lib.SafeLong;
import com.palantir.ri.ResourceIdentifier;
import com.palantir.tokens.auth.AuthHeader;
import com.palantir.tokens.auth.BearerToken;
import java.lang.Boolean;
import java.lang.Double;
import java.lang.Integer;
import java.lang.String;
import java.time.ZonedDateTime;
import java.util.Optional;
import javax.annotation.Generated;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Streaming;

@Generated("com.palantir.conjure.gen.java.services.Retrofit2ServiceGenerator")
public interface EteServiceRetrofit {
    @GET("base/string")
    Call<String> string(@Header("Authorization") AuthHeader authHeader);

    @GET("base/integer")
    Call<Integer> integer(@Header("Authorization") AuthHeader authHeader);

    @GET("base/double")
    Call<Double> double_(@Header("Authorization") AuthHeader authHeader);

    @GET("base/boolean")
    Call<Boolean> boolean_(@Header("Authorization") AuthHeader authHeader);

    @GET("base/safelong")
    Call<SafeLong> safelong(@Header("Authorization") AuthHeader authHeader);

    @GET("base/rid")
    Call<ResourceIdentifier> rid(@Header("Authorization") AuthHeader authHeader);

    @GET("base/bearertoken")
    Call<BearerToken> bearertoken(@Header("Authorization") AuthHeader authHeader);

    @GET("base/optionalString")
    Call<Optional<String>> optionalString(@Header("Authorization") AuthHeader authHeader);

    @GET("base/optionalEmpty")
    Call<Optional<String>> optionalEmpty(@Header("Authorization") AuthHeader authHeader);

    @GET("base/datetime")
    Call<ZonedDateTime> datetime(@Header("Authorization") AuthHeader authHeader);

    @GET("base/binary")
    @Streaming
    Call<ResponseBody> binary(@Header("Authorization") AuthHeader authHeader);
}
