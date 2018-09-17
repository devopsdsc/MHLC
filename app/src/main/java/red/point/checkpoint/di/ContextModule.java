package red.point.checkpoint.di;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import red.point.checkpoint.helper.PrefManager;

@Module
public class ContextModule {
    private final Context context;
    private final Context applicationContext;

    public ContextModule(Context context) {
        this.context = context;
        this.applicationContext = context.getApplicationContext();
    }

    @Provides
    @ContextQualifier
    public Context providesContext() {
        return context;
    }

    @Provides
    @ApplicationContextQualifier
    public Context providesApplicationContext() {
        return applicationContext;
    }

    @Provides
    public PrefManager providesPrefManager(@ApplicationContextQualifier Context context) {
        return new PrefManager(context);
    }
}
