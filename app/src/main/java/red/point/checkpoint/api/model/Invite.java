package red.point.checkpoint.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Invite {

    @SerializedName("id")
    @Expose
    private long id;

    @SerializedName("company_id")
    @Expose
    private long companyId;

    @SerializedName("user_id")
    @Expose
    private long userId;

    @SerializedName("invitee_email")
    @Expose
    private String inviteeEmail;

    @SerializedName("company")
    @Expose
    private Company company;

    public Invite(long id, long companyId, long userId, String inviteeEmail, Company company) {
        this.id = id;
        this.companyId = companyId;
        this.userId = userId;
        this.inviteeEmail = inviteeEmail;
        this.company = company;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(long companyId) {
        this.companyId = companyId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getInviteeEmail() {
        return inviteeEmail;
    }

    public void setInviteeEmail(String inviteeEmail) {
        this.inviteeEmail = inviteeEmail;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
