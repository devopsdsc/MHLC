package red.point.checkpoint.api.service;

import red.point.checkpoint.api.model.PinLocationList;
import red.point.checkpoint.api.model.PinLocationResponse;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PinLocationService {

    @GET("companies/{companyId}/pin-locations")
    Call<PinLocationList> getPinLocationList(
            @Path("companyId") long companyId,
            @Query("limit") long limit,
            @Query("latitude") double latitude,
            @Query("longitude") double longitude,
            @Query("tolerance") double tolerance
    );

    @GET("companies/{companyId}/pin-locations/{pinLocationId}")
    Call<PinLocationResponse> getPinLocation(@Path("companyId") long companyId, @Path("pinLocationId") long pinLocationId);

    @POST("companies/{companyId}/pin-locations")
    @FormUrlEncoded
    Call<PinLocationResponse> storePinLocation(
            @Path("companyId") long companyId,
            @Field("name") String name,
            @Field("address") String address,
            @Field("latitude") double latitude,
            @Field("longitude") double longitude
    );

    @PUT("companies/{companyId}/pin-locations/{pinLocationId}")
    @FormUrlEncoded
    Call<PinLocationResponse> putPinLocation(
            @Path("companyId") long companyId,
            @Path("pinLocationId") long pinLocationId,
            @Field("name") String name,
            @Field("address") String address,
            @Field("latitude") double latitude,
            @Field("longitude") double longitude
    );

    @DELETE("companies/{companyId}/pin-locations/{pinLocationId}")
    Call<PinLocationResponse> deletePinLocation(
            @Path("companyId") long companyId,
            @Path("pinLocationId") long pinLocationId
    );
}
