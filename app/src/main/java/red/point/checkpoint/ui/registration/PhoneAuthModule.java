package red.point.checkpoint.ui.registration;

import dagger.Module;
import dagger.Provides;
import red.point.checkpoint.api.service.UserService;
import red.point.checkpoint.di.ApiModule;
import red.point.checkpoint.di.ContextModule;
import retrofit2.Retrofit;

@Module(includes = {ContextModule.class, ApiModule.class})
public class PhoneAuthModule {

    @Provides
    public UserService userService(Retrofit retrofit) {
        return retrofit.create(UserService.class);
    }
}
