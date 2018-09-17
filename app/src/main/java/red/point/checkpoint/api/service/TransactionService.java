package red.point.checkpoint.api.service;

import red.point.checkpoint.api.model.TransactionResponse;
import red.point.checkpoint.api.model.TransactionList;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface TransactionService {

    @GET("companies/{companyId}/transactions")
    Call<TransactionList> getTransactions(@Path("companyId") long companyId);

    @GET("companies/{companyId}/transactions/{transactionId}")
    Call<TransactionResponse> getTransaction(@Path("companyId") long companyId, @Path("branchId") long branchId);

    @POST("companies/{companyId}/transactions")
    @FormUrlEncoded
    Call<TransactionResponse> storeTransaction(
            @Path("companyId") long companyId,
            @Field("description") String description
    );
}
