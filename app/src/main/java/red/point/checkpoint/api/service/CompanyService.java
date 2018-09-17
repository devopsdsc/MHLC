package red.point.checkpoint.api.service;

import red.point.checkpoint.api.model.CompanyResponse;
import red.point.checkpoint.api.model.TopUpResult;
import red.point.checkpoint.api.model.WalletResponse;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface CompanyService {

    @POST("companies")
    @FormUrlEncoded
    Call<CompanyResponse> storeCompany(
            @Field("name") String name,
            @Field("user_id") long userId,
            @Field("timezone") String timezone
    );

    @PUT("companies/{companyId}")
    @FormUrlEncoded
    Call<CompanyResponse> putCompany(
            @Path("companyId") long companyId,
            @Field("name") String name
    );

    @DELETE("companies/{companyId}")
    Call<CompanyResponse> deleteCompany(
            @Path("companyId") long companyId
    );

    @GET("companies/{companyId}/wallets")
    Call<WalletResponse> getWallet(@Path("companyId") long companyId);

    @POST("companies/{companyId}/top-ups")
    @FormUrlEncoded
    Call<TopUpResult> topUp(@Field("token") String token);

}
