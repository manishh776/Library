package com.kwaou.library.retrofit;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface Api {
    @FormUrlEncoded
    @POST("sendPush.php")
    Call<ResponseBody> sendPush(
            @Field("token") String token,
            @Field("message") String message,
            @Field("type") String type);
}
