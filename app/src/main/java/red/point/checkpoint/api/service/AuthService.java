package red.point.checkpoint.api.service;

import red.point.checkpoint.api.model.LoginResult;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface AuthService {

    @POST("login")
    @FormUrlEncoded
    Call<LoginResult> login(@Field("token") String token);
}
