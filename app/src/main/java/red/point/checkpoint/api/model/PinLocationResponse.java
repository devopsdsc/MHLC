package red.point.checkpoint.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PinLocationResponse {

    @SerializedName("data")
    @Expose
    PinLocation pinLocation;

    @SerializedName("error")
    @Expose
    Error error;

    public PinLocationResponse(PinLocation pinLocation, Error error) {
        this.pinLocation = pinLocation;
        this.error = error;
    }

    public PinLocation getPinLocation() {
        return pinLocation;
    }

    public void setPinLocation(PinLocation pinLocation) {
        this.pinLocation = pinLocation;
    }

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }
}
