package red.point.checkpoint.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CompanyResponse {

    @SerializedName("data")
    @Expose
    private Company company;

    @SerializedName("error")
    @Expose
    private Error error;

    public CompanyResponse(Company company, Error error) {
        this.company = company;
        this.error = error;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }
}
