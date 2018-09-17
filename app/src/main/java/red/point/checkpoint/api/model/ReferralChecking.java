package red.point.checkpoint.api.model;

public class ReferralChecking {

    private boolean isHaveReferral;

    public ReferralChecking(boolean isHaveReferral) {
        this.isHaveReferral = isHaveReferral;
    }

    public boolean isHaveReferral() {
        return isHaveReferral;
    }

    public void setHaveReferral(boolean haveReferral) {
        isHaveReferral = haveReferral;
    }
}
