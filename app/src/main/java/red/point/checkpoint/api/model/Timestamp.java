package red.point.checkpoint.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

@Parcel
public class Timestamp {

    @SerializedName("date")
    @Expose
    String date;

    @SerializedName("timezone_type")
    @Expose
    int timezoneType;

    @SerializedName("timezone")
    @Expose
    String timezone;

    public Timestamp() {
    }

    public Timestamp(String date, int timezoneType, String timezone) {
        this.date = date;
        this.timezoneType = timezoneType;
        this.timezone = timezone;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getTimezoneType() {
        return timezoneType;
    }

    public void setTimezoneType(int timezoneType) {
        this.timezoneType = timezoneType;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
}
