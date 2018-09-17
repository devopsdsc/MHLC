package red.point.checkpoint.api.service;

import red.point.checkpoint.api.model.ReferralSourceList;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ReferralSourceService {

    @GET("referral-source")
    Call<ReferralSourceList> getAll();

}
