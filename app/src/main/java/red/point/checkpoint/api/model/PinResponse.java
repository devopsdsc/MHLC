package red.point.checkpoint.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PinResponse {

    @SerializedName("data")
    @Expose
    private Pin pin;

    @SerializedName("error")
    @Expose
    private Error error;

    public PinResponse(Pin pin, Error error) {
        this.pin = pin;
        this.error = error;
    }

    public Pin getPin() {
        return pin;
    }

    public void setPin(Pin pin) {
        this.pin = pin;
    }

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }
}
