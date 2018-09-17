package red.point.checkpoint.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VoucherResponse {

    @SerializedName("message")
    @Expose
    private String message;

    @SerializedName("value")
    @Expose
    private double value;

    @SerializedName("error")
    @Expose
    private Error error;

    public VoucherResponse(String message, double value, Error error) {
        this.message = message;
        this.value = value;
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
