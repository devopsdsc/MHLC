package red.point.checkpoint.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import red.point.checkpoint.MyApplication;

public class ToastUtil {

    public static void show(final String sText) {
        final Context context = MyApplication.getAppContext();

        new Handler(Looper.getMainLooper()).post(() -> {
            Toast toast = Toast.makeText(context, sText, Toast.LENGTH_SHORT);
            toast.show();
        });
    }

    public static void showConnectionFailure() {
        final Context context = MyApplication.getAppContext();

        new Handler(Looper.getMainLooper()).post(() -> {
            Toast toast = Toast.makeText(context, "Connection Failure", Toast.LENGTH_LONG);
            toast.show();
        });
    }
}
