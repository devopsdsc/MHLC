package red.point.checkpoint.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PinLocationList {

    @SerializedName("data")
    @Expose
    private List<PinLocation> pinLocations;

    public PinLocationList(List<PinLocation> pinLocations) {
        this.pinLocations = pinLocations;
    }

    public List<PinLocation> getPinLocations() {
        return pinLocations;
    }

    public void setPinLocations(List<PinLocation> pinLocations) {
        this.pinLocations = pinLocations;
    }
}
