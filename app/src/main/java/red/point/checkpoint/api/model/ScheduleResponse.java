package red.point.checkpoint.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ScheduleResponse {

    @SerializedName("data")
    @Expose
    private Schedule schedule;

    @SerializedName("error")
    @Expose
    private Error error;

    public ScheduleResponse(Schedule schedule, Error error) {
        this.schedule = schedule;
        this.error = error;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }
}
