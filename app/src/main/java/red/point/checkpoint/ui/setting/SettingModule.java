package red.point.checkpoint.ui.setting;

import dagger.Module;
import dagger.Provides;
import red.point.checkpoint.api.service.SettingService;
import red.point.checkpoint.di.ApiModule;
import red.point.checkpoint.di.ContextModule;
import retrofit2.Retrofit;

@Module(includes = {ContextModule.class, ApiModule.class})
public class SettingModule {

    @Provides
    public SettingService settingService(Retrofit retrofit) {
        return retrofit.create(SettingService.class);
    }
}
