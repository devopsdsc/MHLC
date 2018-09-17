package red.point.checkpoint.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ShiftResponse {

    @SerializedName("data")
    @Expose
    private Shift shift;

    @SerializedName("error")
    @Expose
    private Error error;

    public ShiftResponse(Shift shift, Error error) {
        this.shift = shift;
        this.error = error;
    }

    public Shift getShift() {
        return shift;
    }

    public void setShift(Shift shift) {
        this.shift = shift;
    }

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }
}
