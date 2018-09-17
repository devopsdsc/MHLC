package red.point.checkpoint.api.service;

import red.point.checkpoint.api.model.ShiftList;
import red.point.checkpoint.api.model.ShiftResponse;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ShiftService {

    @GET("companies/{companyId}/shifts")
    Call<ShiftList> getShifts(@Path("companyId") long companyId);

    @GET("companies/{companyId}/shifts/{shiftId}")
    Call<ShiftResponse> getShift(@Path("companyId") long companyId, @Path("shiftId") long shiftId);

    @POST("companies/{companyId}/shifts")
    @FormUrlEncoded
    Call<ShiftResponse> storeShift(
            @Path("companyId") long companyId,
            @Field("name") String name,
            @Field("shift_start") String shiftStart,
            @Field("shift_end") String shiftEnd
    );

    @PUT("companies/{companyId}/shifts/{shiftId}")
    @FormUrlEncoded
    Call<ShiftResponse> putShift(
            @Path("companyId") long companyId,
            @Path("shiftId") long shiftId,
            @Field("name") String name,
            @Field("shift_start") String shiftStart,
            @Field("shift_end") String shiftEnd
    );
}
