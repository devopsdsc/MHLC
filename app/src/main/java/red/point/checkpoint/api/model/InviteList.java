package red.point.checkpoint.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class InviteList {

    @SerializedName("data")
    @Expose
    private List<Invite> invites;

    public InviteList(List<Invite> invites) {
        this.invites = invites;
    }

    public List<Invite> getInvites() {
        return invites;
    }

    public void setInvites(List<Invite> invites) {
        this.invites = invites;
    }
}
