package red.point.checkpoint.api.service;

import red.point.checkpoint.api.model.ReferralChecking;
import red.point.checkpoint.api.model.ReferralResponse;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ReferralService {

    @GET("referral/user/{userId}")
    Call<ReferralChecking> isHaveReferral(@Path("userId") long userId);

    @POST("referral")
    @FormUrlEncoded
    Call<ReferralResponse> store(
            @Field("user_id") Long userId,
            @Field("referral_code") String referralCode,
            @Field("referral_source_id") Long referralSourceId
    );

}
