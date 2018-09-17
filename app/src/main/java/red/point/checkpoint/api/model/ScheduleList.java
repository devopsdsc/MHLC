package red.point.checkpoint.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ScheduleList {

    @SerializedName("data")
    @Expose
    private List<Schedule> schedules;

    @SerializedName("meta")
    @Expose
    private ScheduleListMeta meta;

    public ScheduleList(List<Schedule> schedules, ScheduleListMeta meta) {
        this.schedules = schedules;
        this.meta = meta;
    }

    public List<Schedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<Schedule> schedules) {
        this.schedules = schedules;
    }

    public ScheduleListMeta getMeta() {
        return meta;
    }

    public void setMeta(ScheduleListMeta meta) {
        this.meta = meta;
    }
}
