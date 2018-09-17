package red.point.checkpoint.api.service;

import red.point.checkpoint.api.model.NotificationList;
import red.point.checkpoint.api.model.NotificationResponse;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface NotificationService {

    @GET("companies/{companyId}/notifications")
    Call<NotificationList> getNotifications(@Path("companyId") long companyId);

    @POST("companies/{companyId}/notifications")
    @FormUrlEncoded
    Call<NotificationResponse> storeNotification(
            @Path("companyId") long companyId,
            @Field("user_id") long userId,
            @Field("date") String date,
            @Field("time") String time,
            @Field("message") String message
    );
}
