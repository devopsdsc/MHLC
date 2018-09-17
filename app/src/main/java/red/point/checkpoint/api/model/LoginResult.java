package red.point.checkpoint.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LoginResult {

    @SerializedName("token")
    @Expose
    private String token;

    @SerializedName("companies")
    @Expose
    private List<Company> companies;

    @SerializedName("user")
    @Expose
    private User user;

    public LoginResult(String token, List<Company> companies, User user) {
        this.token = token;
        this.companies = companies;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public List<Company> getCompanies() {
        return companies;
    }

    public void setCompanies(List<Company> companies) {
        this.companies = companies;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
