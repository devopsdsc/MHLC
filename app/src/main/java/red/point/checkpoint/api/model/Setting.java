package red.point.checkpoint.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

@Parcel
public class Setting {

    @SerializedName("id")
    @Expose
    public long id;

    @SerializedName("company_id")
    @Expose
    public long companyId;

    @SerializedName("check_in_radius")
    @Expose
    public int checkInRadius;

    @SerializedName("late_range")
    @Expose
    public int lateRange;

    @SerializedName("late_charge")
    @Expose
    public double lateCharge;

    @SerializedName("max_charge")
    @Expose
    public double maxCharge;

    @SerializedName("reward")
    @Expose
    public double reward;

    public Setting() {
    }

    public Setting(long id, long companyId, int checkInRadius, int lateRange, double lateCharge, double maxCharge, double reward) {
        this.id = id;
        this.companyId = companyId;
        this.checkInRadius = checkInRadius;
        this.lateRange = lateRange;
        this.lateCharge = lateCharge;
        this.maxCharge = maxCharge;
        this.reward = reward;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(long companyId) {
        this.companyId = companyId;
    }

    public int getCheckInRadius() {
        return checkInRadius;
    }

    public void setCheckInRadius(int checkInRadius) {
        this.checkInRadius = checkInRadius;
    }

    public int getLateRange() {
        return lateRange;
    }

    public void setLateRange(int lateRange) {
        this.lateRange = lateRange;
    }

    public double getLateCharge() {
        return lateCharge;
    }

    public void setLateCharge(double lateCharge) {
        this.lateCharge = lateCharge;
    }

    public double getMaxCharge() {
        return maxCharge;
    }

    public void setMaxCharge(double maxCharge) {
        this.maxCharge = maxCharge;
    }

    public double getReward() {
        return reward;
    }

    public void setReward(double reward) {
        this.reward = reward;
    }
}
