package red.point.checkpoint.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TopUpResult {

    @SerializedName("top_up")
    @Expose
    private TopUp topUp;

    public TopUpResult(TopUp topUp) {
        this.topUp = topUp;
    }

    public TopUp getTopUp() {
        return topUp;
    }

    public void setTopUp(TopUp topUp) {
        this.topUp = topUp;
    }
}
