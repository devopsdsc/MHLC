package red.point.checkpoint.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PermissionManager {

    private List<String> mPermissionsToRequest = new ArrayList<>();

    public PermissionManager() {}

    /**
     * Checks if all the required permissions are granted.
     * @return true if all the required permissions are granted, otherwise false
     */
    public boolean hasPermissions(Context context, String... requiredPermissions) {

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            return true;
        }

        for (String permission : requiredPermissions) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                mPermissionsToRequest.add(permission);
            }
        }
        return mPermissionsToRequest.isEmpty();
    }

    /**
     * Requests the missing permissions.
     * The activity from which this method is called has to implement
     * {@link Activity#onRequestPermissionsResult(int, String[], int[])}
     * and then, inside it, it has to call the method
     * {@link AndroidPermissions#areAllRequiredPermissionsGranted(String[], int[])} to check that all the
     * requested permissions are granted by the user
     * @param requestCode request code used by the activity
     */
    public void requestPermissions(Activity activity, int requestCode) {
        ActivityCompat.requestPermissions(activity, getPermissionsToRequest(), requestCode);
    }

    public String[] getPermissionsToRequest() {
        return mPermissionsToRequest.toArray(new String[mPermissionsToRequest.size()]);
    }

    /**
     * Method to call inside
     * {@link Activity#onRequestPermissionsResult(int, String[], int[])}, to check if the
     * required permissions are granted.
     * @param permissions permissions requested
     * @param grantResults results
     * @return true if all the required permissions are granted, otherwise false
     */
    public boolean areAllRequiredPermissionsGranted(String[] permissions, int[] grantResults) {
        if (permissions == null || permissions.length == 0
                || grantResults == null || grantResults.length == 0) {
            return false;
        }

        LinkedHashMap<String, Integer> perms = new LinkedHashMap<>();

        for (int i = 0; i < permissions.length; i++) {
            if (!perms.containsKey(permissions[i])
                    || (perms.containsKey(permissions[i]) && perms.get(permissions[i]) == PackageManager.PERMISSION_DENIED))
                perms.put(permissions[i], grantResults[i]);
        }

        for (Map.Entry<String, Integer> entry : perms.entrySet()) {
            if (entry.getValue() != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

}
