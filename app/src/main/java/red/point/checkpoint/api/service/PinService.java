package red.point.checkpoint.api.service;

import red.point.checkpoint.api.model.PinList;
import red.point.checkpoint.api.model.PinResponse;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PinService {

    @GET("companies/{companyId}/pins")
    Call<PinList> getPins(
            @Path("companyId") long companyId,
            @Query("user_id") long userId,
            @Query("start_date") String startDate,
            @Query("end_date") String endDate);

    @GET("companies/{companyId}/pins/{pinId}")
    Call<PinResponse> getPin(
            @Path("companyId") long companyId,
            @Path("pinId") long pinId);

    @POST("companies/{companyId}/pins")
    @FormUrlEncoded
    Call<PinResponse> storePin(
            @Path("companyId") long companyId,
            @Field("pin_location_id") Long pinLocationId,
            @Field("user_id") long user_id,
            @Field("latitude") double latitude,
            @Field("longitude") double longitude
    );

    @PUT("companies/{companyId}/pins/{pin_id}")
    @FormUrlEncoded
    Call<PinResponse> putPin(
            @Path("companyId") long companyId,
            @Path("pin_id") long pinId,
            @Field("pin_location_id") Long pinLocationId,
            @Field("user_id") long user_id,
            @Field("address") String address,
            @Field("latitude") double latitude,
            @Field("longitude") double longitude
    );

    @DELETE("companies/{companyId}/pins/{pin_id}")
    Call<PinResponse> deletePin(
            @Path("companyId") long companyId,
            @Field("pin_id") long pinId
    );
}
