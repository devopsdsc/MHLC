package red.point.checkpoint.api.service;

import red.point.checkpoint.api.model.CompanyList;
import red.point.checkpoint.api.model.UserList;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface UserCompanyService {

    @GET("users/{userId}/companies")
    Call<CompanyList> getCompanies(@Path("userId") long userId);

    @GET("companies/{companyId}/users")
    Call<UserList> getUsers(@Path("companyId") long companyId);
}
