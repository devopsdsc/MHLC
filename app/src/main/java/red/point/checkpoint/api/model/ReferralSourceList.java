package red.point.checkpoint.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ReferralSourceList {

    @SerializedName("data")
    @Expose
    private List<ReferralSource> referralSourceList;

    public ReferralSourceList(List<ReferralSource> referralSourceList) {
        this.referralSourceList = referralSourceList;
    }

    public List<ReferralSource> getReferralSourceList() {
        return referralSourceList;
    }

    public void setReferralSourceList(List<ReferralSource> referralSourceList) {
        this.referralSourceList = referralSourceList;
    }
}
