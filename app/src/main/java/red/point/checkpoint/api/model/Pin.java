package red.point.checkpoint.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

@Parcel
public class Pin {

    @SerializedName("id")
    @Expose
    long id;

    @SerializedName("company_id")
    @Expose
    long companyId;

    @SerializedName("user_id")
    @Expose
    long userId;

    @SerializedName("employee")
    @Expose
    Employee employee;

    @SerializedName("date")
    @Expose
    String date;

    @SerializedName("address")
    @Expose
    String address;

    @SerializedName("created_at")
    @Expose
    Timestamp createdAt;

    @SerializedName("latitude")
    @Expose
    double latitude;

    @SerializedName("longitude")
    @Expose
    double longitude;

    @SerializedName("pin_location")
    @Expose
    PinLocation pinLocation;

    public Pin() {}

    public Pin(long id, long companyId, long userId, Employee employee, String date, String address, Timestamp createdAt, double latitude, double longitude, PinLocation pinLocation) {
        this.id = id;
        this.companyId = companyId;
        this.userId = userId;
        this.employee = employee;
        this.date = date;
        this.address = address;
        this.createdAt = createdAt;
        this.latitude = latitude;
        this.longitude = longitude;
        this.pinLocation = pinLocation;
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

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public PinLocation getPinLocation() {
        return pinLocation;
    }

    public void setPinLocation(PinLocation pinLocation) {
        this.pinLocation = pinLocation;
    }
}
