package red.point.checkpoint.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PinList {

    @SerializedName("data")
    @Expose
    private List<Pin> pins;

    public PinList(List<Pin> pins) {
        this.pins = pins;
    }

    public List<Pin> getPins() {
        return pins;
    }

    public void setPins(List<Pin> pins) {
        this.pins = pins;
    }
}
