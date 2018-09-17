package red.point.checkpoint.di;

import android.app.Activity;
import android.content.Context;

import dagger.Module;
import dagger.Provides;

@Module
public class ActivityModule {
    private final Activity context;

    public ActivityModule(Activity context) {
        this.context = context;
    }

    @Provides
    @ActivityContextQualifier
    public Context context() {
        return context;
    }
}
