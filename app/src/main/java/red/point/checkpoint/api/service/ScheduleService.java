package red.point.checkpoint.api.service;

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

public interface ScheduleService {

    @GET("companies/{companyId}/schedules-by-user/{userId}")
    Call<ScheduleList> getSchedulesByUser(
            @Path("companyId") long companyId,
            @Path("userId") long userId,
            @Query("start_date") String startDate,
            @Query("end_date") String endDate);

    @GET("companies/{companyId}/schedules-by-user/{userId}/today")
    Call<ScheduleList> getTodaySchedulesByUser(
            @Path("companyId") long companyId,
            @Path("userId") long userId,
            @Query("start_date") String startDate,
            @Query("end_date") String endDate);

    @GET("companies/{companyId}/schedules/{scheduleId}")
    Call<ScheduleResponse> getSchedule(
            @Path("companyId") long companyId,
            @Path("scheduleId") long scheduleId);

    @POST("companies/{companyId}/schedules")
    @FormUrlEncoded
    Call<ScheduleResponse> storeSchedule(
            @Path("companyId") long companyId,
            @Field("user_id") long userId,
            @Field("branch_id") long branchId,
            @Field("date") String date,
            @Field("shift_start") String shift_start,
            @Field("shift_end") String shift_end);

    @PUT("companies/{companyId}/schedules/{scheduleId}")
    @FormUrlEncoded
    Call<ScheduleResponse> putSchedule(
            @Path("companyId") long companyId,
            @Path("scheduleId") long scheduleId,
            @Field("name") String name,
            @Field("address") String address,
            @Field("latitude") double latitude,
            @Field("longitude") double longitude);

    @PUT("companies/{companyId}/schedules/{scheduleId}/check-in")
    Call<ScheduleResponse> checkIn(
            @Path("companyId") long companyId,
            @Path("scheduleId") long scheduleId);

    @PUT("companies/{companyId}/schedules/{scheduleId}/check-out")
    Call<ScheduleResponse> checkOut(
            @Path("companyId") long companyId,
            @Path("scheduleId") long scheduleId);

    @DELETE("companies/{companyId}/schedules/{scheduleId}")
    Call<ScheduleResponse> deleteSchedule(
            @Path("companyId") long companyId,
            @Path("scheduleId") long scheduleId);
}
