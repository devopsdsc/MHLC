package red.point.checkpoint.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

@Parcel
public class Notification {

    @SerializedName("id")
    @Expose
    long id;

    @SerializedName("company_id")
    @Expose
    long companyId;

    @SerializedName("user_id")
    @Expose
    long userId;

    @SerializedName("body")
    @Expose
    String message;

    @SerializedName("published_date")
    @Expose
    String date;

    public Notification() {
    }

    public Notification(long id, long companyId, long userId, String message, String date) {
        this.id = id;
        this.companyId = companyId;
        this.userId = userId;
        this.message = message;
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
