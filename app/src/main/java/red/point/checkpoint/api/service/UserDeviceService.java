package red.point.checkpoint.api.service;

import red.point.checkpoint.api.model.DeviceResponse;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface UserDeviceService {

    @POST("users/{userId}/devices")
    @FormUrlEncoded
    Call<DeviceResponse> storeDevice(
            @Path("userId") long userId,
            @Field("phone_brand") String phoneBrand,
            @Field("phone_model") String phoneModel,
            @Field("phone_manufacture") String phoneManufacture,
            @Field("phone_serial") String phoneSerial,
            @Field("version_code") long versionCode,
            @Field("version_name") String versionName);

}
