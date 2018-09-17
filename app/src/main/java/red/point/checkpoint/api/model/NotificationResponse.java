package red.point.checkpoint.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class NotificationResponse {

    @SerializedName("data")
    @Expose
    private Notification notification;

    @SerializedName("error")
    @Expose
    private Error error;

    public NotificationResponse() {
    }

    public NotificationResponse(Notification notification, Error error) {
        this.notification = notification;
        this.error = error;
    }

    public Notification getNotification() {
        return notification;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }
}
