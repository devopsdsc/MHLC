package red.point.checkpoint;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;

import java.io.File;

import io.fabric.sdk.android.Fabric;

public class MyApplication extends MultiDexApplication {

    private static MyApplication mInstance;
    private static Context context;
    private Activity activity = null;

    public static synchronized MyApplication getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (! BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }
        MyApplication.context = getApplicationContext();

        MultiDex.install(this);

        mInstance = this;
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public static File getCacheDirectory() {
        File cache = context.getCacheDir();

        if (!cache.exists()) {
            cache.mkdirs();
        }

        return cache;
    }

    public static boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = null;

        if (cm != null) {
            activeNetwork = cm.getActiveNetworkInfo();
        }

        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}