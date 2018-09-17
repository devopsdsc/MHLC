package red.point.checkpoint.api.service;

import red.point.checkpoint.api.model.SettingResponse;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface SettingService {

    @GET("companies/{companyId}/settings")
    Call<SettingResponse> getSetting(@Path("companyId") long companyId);

    @PUT("companies/{companyId}/settings")
    @FormUrlEncoded
    Call<SettingResponse> putSetting(
            @Path("companyId") long companyId,
            @Field("check_in_radius") int checkInRadius,
            @Field("late_range") int lateRange,
            @Field("late_charge") double salary,
            @Field("max_charge") double maxCharge,
            @Field("reward") double reward
    );
}
