package red.point.checkpoint.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Device {

    @SerializedName("id")
    @Expose
    private long id;

    @SerializedName("user_id")
    @Expose
    private long userId;

    @SerializedName("phone_brand")
    @Expose
    private String phoneBrand;

    @SerializedName("phone_manufacturer")
    @Expose
    private String phoneManufacturer;

    @SerializedName("phone_model")
    @Expose
    private String phoneModel;

    @SerializedName("phone_serial")
    @Expose
    private String phoneSerial;

    @SerializedName("version_name")
    @Expose
    private String versionName;

    @SerializedName("version_code")
    @Expose
    private int versionCode;

    public Device() {}

    public Device(long id, long userId, String phoneBrand, String phoneManufacturer, String phoneModel, String phoneSerial, String versionName, int versionCode) {
        this.id = id;
        this.userId = userId;
        this.phoneBrand = phoneBrand;
        this.phoneManufacturer = phoneManufacturer;
        this.phoneModel = phoneModel;
        this.phoneSerial = phoneSerial;
        this.versionName = versionName;
        this.versionCode = versionCode;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPhoneBrand() {
        return phoneBrand;
    }

    public void setPhoneBrand(String phoneBrand) {
        this.phoneBrand = phoneBrand;
    }

    public String getPhoneManufacturer() {
        return phoneManufacturer;
    }

    public void setPhoneManufacturer(String phoneManufacturer) {
        this.phoneManufacturer = phoneManufacturer;
    }

    public String getPhoneModel() {
        return phoneModel;
    }

    public void setPhoneModel(String phoneModel) {
        this.phoneModel = phoneModel;
    }

    public String getPhoneSerial() {
        return phoneSerial;
    }

    public void setPhoneSerial(String phoneSerial) {
        this.phoneSerial = phoneSerial;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
}
