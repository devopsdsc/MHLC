package red.point.checkpoint.api.service;

import red.point.checkpoint.api.model.EmployeeList;
import red.point.checkpoint.api.model.EmployeeResponse;
import red.point.checkpoint.api.model.Invite;
import red.point.checkpoint.api.model.InviteList;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface EmployeeService {

    @GET("companies/{companyId}/employees")
    Call<EmployeeList> getEmployees(@Path("companyId") long companyId);

    @GET("companies/{companyId}/employees/{userId}")
    Call<EmployeeResponse> getEmployee(@Path("companyId") long companyId, @Path("userId") long userId);

    @PUT("companies/{companyId}/employees/{userId}")
    @FormUrlEncoded
    Call<EmployeeResponse> putEmployee(
            @Path("companyId") long companyId,
            @Path("userId") long userId,
            @Field("name") String name,
            @Field("salary_type") String salaryType,
            @Field("salary") double salary,
            @Field("is_admin") boolean isAdmin,
            @Field("is_owner") boolean isOwner
    );

    @DELETE("companies/{companyId}/employees/{userId}")
    Call<EmployeeResponse> deleteEmployee(
            @Path("companyId") long companyId,
            @Path("userId") long userId
    );

    @GET("companies/{companyId}/invites")
    Call<InviteList> getInvites(@Path("companyId") long companyId);

    @POST("companies/{companyId}/invites")
    @FormUrlEncoded
    Call<Invite> invite(
            @Path("companyId") long companyId,
            @Field("user_id") long userId,
            @Field("invitee_email") String inviteeEmail);

    @GET("invites/index")
    Call<InviteList> invited(@Query("invitee_email") String inviteeEmail);

    @POST("companies/{companyId}/invites/{inviteId}/join")
    Call<Invite> joinCompany(
            @Path("companyId") long companyId,
            @Path("inviteId") long inviteId);
}
