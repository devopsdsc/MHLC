package red.point.checkpoint.util;

import android.app.ProgressDialog;
import android.content.Context;

import red.point.checkpoint.MyApplication;

/**
 * ProgressDialogUtil create default configuration ProgressDialog and
 * contains some methods that you can call to show and dismiss ProgressDialog
 */
public class ProgressDialogUtil {

    private static final String TAG = ProgressDialogUtil.class.getSimpleName();

    private static ProgressDialog mProgressDialog;

    public static void show(Context context, String message) {
        if (MyApplication.getInstance().getActivity() == null) {
            return;
        }

        if (MyApplication.getInstance().getActivity().isFinishing()) {
            return;
        }

        if (mProgressDialog == null || !mProgressDialog.isShowing()) {
            mProgressDialog = new ProgressDialog(context);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();
        }

        mProgressDialog.setMessage(message);
    }

    public static void showLoading(Context context) {
        show(context, "Loading");
    }

    public static void setMessage(String message) {
        if (mProgressDialog == null) {
            return;
        }

        if (mProgressDialog.isShowing()) {
            mProgressDialog.setMessage(message);
        }
    }

    public static void dismiss() {
        if (mProgressDialog == null) {
            return;
        }

        if (mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
}
