package red.point.checkpoint.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static red.point.checkpoint.api.ApiStatus.ERROR;
import static red.point.checkpoint.api.ApiStatus.LOADING;
import static red.point.checkpoint.api.ApiStatus.SUCCESS;

public class ApiResponse<T> {
    @NonNull
    public final ApiStatus status;

    @Nullable
    public final String message;

    @Nullable
    public final T data;

    public ApiResponse(@NonNull ApiStatus status, @Nullable T data, @Nullable String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    public static <T> ApiResponse<T> success(@Nullable T data) {
        return new ApiResponse<>(SUCCESS, data, null);
    }

    public static <T> ApiResponse<T> error(String msg, @Nullable T data) {
        return new ApiResponse<>(ERROR, data, msg);
    }

    public static <T> ApiResponse<T> loading(@Nullable T data) {
        return new ApiResponse<>(LOADING, data, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ApiResponse<?> resource = (ApiResponse<?>) o;

        if (status != resource.status) {
            return false;
        }
        if (message != null ? !message.equals(resource.message) : resource.message != null) {
            return false;
        }
        return data != null ? data.equals(resource.data) : resource.data == null;
    }

    @Override
    public int hashCode() {
        int result = status.hashCode();
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (data != null ? data.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }

}
