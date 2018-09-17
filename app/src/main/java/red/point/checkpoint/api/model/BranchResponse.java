package red.point.checkpoint.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BranchResponse {

    @SerializedName("data")
    @Expose
    private Branch branch;

    @SerializedName("error")
    @Expose
    private Error error;

    public BranchResponse(Branch branch, Error error) {
        this.branch = branch;
        this.error = error;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }
}
