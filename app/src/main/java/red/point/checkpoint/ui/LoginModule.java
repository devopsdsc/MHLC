package red.point.checkpoint.ui;

import dagger.Module;
import dagger.Provides;
import red.point.checkpoint.api.service.AuthService;
import red.point.checkpoint.api.service.UserDeviceService;
import red.point.checkpoint.di.ApiModule;
import red.point.checkpoint.di.ContextModule;
import retrofit2.Retrofit;

@Module(includes = {ContextModule.class, ApiModule.class})
public class LoginModule {

    @Provides
    public AuthService authService(Retrofit retrofit) {
        return retrofit.create(AuthService.class);
    }

    @Provides
    public UserDeviceService userDeviceService(Retrofit retrofit) {
        return retrofit.create(UserDeviceService.class);
    }
}
