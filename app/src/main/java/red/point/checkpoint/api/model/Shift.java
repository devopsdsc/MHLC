package red.point.checkpoint.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

@Parcel
public class Shift {

    @SerializedName("id")
    @Expose
    long id;

    @SerializedName("company_id")
    @Expose
    long companyId;

    @SerializedName("name")
    @Expose
    String name;

    @SerializedName("shift_start")
    @Expose
    String shiftStart;

    @SerializedName("shift_end")
    @Expose
    String shiftEnd;

    public Shift() {
    }

    public Shift(long id, long companyId, String name, String shiftStart, String shiftEnd) {
        this.id = id;
        this.companyId = companyId;
        this.name = name;
        this.shiftStart = shiftStart;
        this.shiftEnd = shiftEnd;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShiftStart() {
        return shiftStart;
    }

    public void setShiftStart(String shiftStart) {
        this.shiftStart = shiftStart;
    }

    public String getShiftEnd() {
        return shiftEnd;
    }

    public void setShiftEnd(String shiftEnd) {
        this.shiftEnd = shiftEnd;
    }
}
