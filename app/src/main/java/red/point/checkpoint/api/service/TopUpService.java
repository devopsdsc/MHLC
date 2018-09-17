package red.point.checkpoint.api.service;

import red.point.checkpoint.api.model.TopUpResult;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface TopUpService {

    @POST("top-ups")
    @FormUrlEncoded
    Call<TopUpResult> topUp(
            @Field("company_id") long companyId,
            @Field("user_id") long userId,
            @Field("orderId") String orderId,
            @Field("itemType") String itemType,
            @Field("signature") String signature,
            @Field("purchaseState") int purchaseState,
            @Field("purchaseTime") long purchaseTime,
            @Field("developerPayload") String developerPayload,
            @Field("sku") String sku,
            @Field("token") String token,
            @Field("value") double value);
}
