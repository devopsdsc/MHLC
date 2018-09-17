package red.point.checkpoint.api.service;

import red.point.checkpoint.api.model.UserResponse;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface UserService {

    @GET("users/{userId}")
    Call<UserResponse> getUser(@Path("userId") long userId);

    @POST("users")
    @FormUrlEncoded
    Call<UserResponse> createUser(
            @Field("name") String name,
            @Field("email") String email);

    @PUT("users/{userId}")
    @FormUrlEncoded
    Call<UserResponse> updateUser(
            @Path("userId") long userId,
            @Field("name") String name,
            @Field("email") String email,
            @Field("phone") String phone,
            @Field("firebase_token") String firebaseToken);
}
