package red.point.checkpoint.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

@Parcel
public class Schedule {

    @SerializedName("id")
    @Expose
    long id;

    @SerializedName("company_id")
    @Expose
    long companyId;

    @SerializedName("branch_id")
    @Expose
    long branchId;

    @SerializedName("user_id")
    @Expose
    long userId;

    @SerializedName("date")
    @Expose
    String date;

    @SerializedName("shift_start")
    @Expose
    String shiftStart;

    @SerializedName("shift_end")
    @Expose
    String shiftEnd;

    @SerializedName("check_in")
    @Expose
    Long checkIn;

    @SerializedName("check_out")
    @Expose
    Long checkOut;

    @SerializedName("check_in_late")
    @Expose
    Long checkInLate;

    @SerializedName("check_out_late")
    @Expose
    Long checkOutLate;

    @SerializedName("charge")
    @Expose
    Long charge;

    @SerializedName("reward")
    @Expose
    Long reward;

    @SerializedName("salary")
    @Expose
    Long salary;

    @SerializedName("branch")
    @Expose
    Branch branch;

    @SerializedName("employee")
    @Expose
    Employee employee;

    public Schedule() {
    }

    public Schedule(long id, long companyId, long branchId, long userId, String date, String shiftStart, String shiftEnd, Long checkIn, Long checkOut, Long checkInLate, Long checkOutLate, Long charge, Long reward, Long salary, Branch branch, Employee employee) {
        this.id = id;
        this.companyId = companyId;
        this.branchId = branchId;
        this.userId = userId;
        this.date = date;
        this.shiftStart = shiftStart;
        this.shiftEnd = shiftEnd;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.checkInLate = checkInLate;
        this.checkOutLate = checkOutLate;
        this.charge = charge;
        this.reward = reward;
        this.salary = salary;
        this.branch = branch;
        this.employee = employee;
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

    public long getBranchId() {
        return branchId;
    }

    public void setBranchId(long branchId) {
        this.branchId = branchId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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

    public Long getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(Long checkIn) {
        this.checkIn = checkIn;
    }

    public Long getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(Long checkOut) {
        this.checkOut = checkOut;
    }

    public Long getCheckInLate() {
        return checkInLate;
    }

    public void setCheckInLate(Long checkInLate) {
        this.checkInLate = checkInLate;
    }

    public Long getCheckOutLate() {
        return checkOutLate;
    }

    public void setCheckOutLate(Long checkOutLate) {
        this.checkOutLate = checkOutLate;
    }

    public Long getCharge() {
        return charge;
    }

    public void setCharge(Long charge) {
        this.charge = charge;
    }

    public Long getReward() {
        return reward;
    }

    public void setReward(Long reward) {
        this.reward = reward;
    }

    public Long getSalary() {
        return salary;
    }

    public void setSalary(Long salary) {
        this.salary = salary;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
}
