package red.point.checkpoint.api.service;

import red.point.checkpoint.api.model.BranchList;
import red.point.checkpoint.api.model.BranchResponse;
import red.point.checkpoint.api.model.ScheduleList;
import red.point.checkpoint.api.model.ScheduleResponse;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface BranchService {

    @GET("companies/{companyId}/branches")
    Call<BranchList> getBranchList(@Path("companyId") long companyId);

    @GET("companies/{companyId}/branches/{branchId}")
    Call<BranchResponse> getBranch(@Path("companyId") long companyId, @Path("branchId") long branchId);

    @GET("companies/{companyId}/branches/{branchId}/schedule")
    Call<ScheduleList> getSchedules(@Path("companyId") long companyId, @Path("branchId") long branchId, @Query("date") String date);

    @POST("companies/{companyId}/branches")
    @FormUrlEncoded
    Call<BranchResponse> storeBranch(
        @Path("companyId") long companyId,
        @Field("name") String name,
        @Field("address") String address,
        @Field("latitude") double latitude,
        @Field("longitude") double longitude
    );

    @PUT("companies/{companyId}/branches/{branchId}")
    @FormUrlEncoded
    Call<BranchResponse> putBranch(
            @Path("companyId") long companyId,
            @Path("branchId") long branchId,
            @Field("name") String name,
            @Field("address") String address,
            @Field("latitude") double latitude,
            @Field("longitude") double longitude
    );

    @DELETE("companies/{companyId}/branches/{branchId}")
    Call<BranchResponse> deleteBranch(
        @Path("companyId") long companyId,
        @Path("branchId") long branchId
    );

    @DELETE("companies/{companyId}/branches/{branchId}/schedule/{scheduleId}")
    Call<ScheduleResponse> deleteSchedule(
            @Path("companyId") long companyId,
            @Path("branchId") long branchId,
            @Path("scheduleId") long scheduleId
    );
}
