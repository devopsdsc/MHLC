package red.point.checkpoint.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import red.point.checkpoint.ui.MainActivity;

public class GpsLocationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (intent.getAction().matches("android.location.PROVIDERS_CHANGED"))
        {
            Intent i = new Intent(context, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(i);
        }
    }
}
