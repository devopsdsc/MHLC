package red.point.checkpoint.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ReferralResponse {

    @SerializedName("data")
    @Expose
    private Referral referral;

    @SerializedName("error")
    @Expose
    private Error error;

    public ReferralResponse() {
    }

    public ReferralResponse(Referral referral, Error error) {
        this.referral = referral;
        this.error = error;
    }

    public Referral getReferral() {
        return referral;
    }

    public void setReferral(Referral referral) {
        this.referral = referral;
    }

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }
}
