package red.point.checkpoint.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

@Parcel
public class ScheduleListMeta {

    @SerializedName("setting")
    @Expose
    Setting setting;

    public ScheduleListMeta() {
    }

    public ScheduleListMeta(Setting setting) {
        this.setting = setting;
    }

    public Setting getSetting() {
        return setting;
    }

    public void setSetting(Setting setting) {
        this.setting = setting;
    }
}
