package com.palantir.product;

import java.lang.Boolean;
import javax.annotation.Generated;
import retrofit2.Call;
import retrofit2.http.GET;

@Generated("com.palantir.conjure.gen.java.services.Retrofit2ServiceGenerator")
public interface EmptyPathServiceRetrofit {
    @GET("./")
    Call<Boolean> emptyPath();
}
