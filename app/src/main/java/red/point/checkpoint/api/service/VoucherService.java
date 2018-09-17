package red.point.checkpoint.api.service;

import red.point.checkpoint.api.model.VoucherResponse;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface VoucherService {

    @POST("use-voucher/{companyId}")
    @FormUrlEncoded
    Call<VoucherResponse> useVoucher(
            @Path("companyId") long companyId,
            @Field("code") String code
    );
}
