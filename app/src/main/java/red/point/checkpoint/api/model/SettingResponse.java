package red.point.checkpoint.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SettingResponse {

    @SerializedName("data")
    @Expose
    private Setting setting;

    @SerializedName("error")
    @Expose
    private Error error;

    public SettingResponse(Setting setting, Error error) {
        this.setting = setting;
        this.error = error;
    }

    public Setting getSetting() {
        return setting;
    }

    public void setSetting(Setting setting) {
        this.setting = setting;
    }

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }
}
