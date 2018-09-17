package red.point.checkpoint;

import dagger.Module;
import dagger.Provides;
import red.point.checkpoint.api.service.ReferralService;
import red.point.checkpoint.api.service.UserService;
import red.point.checkpoint.di.ApiModule;
import red.point.checkpoint.di.ContextModule;
import retrofit2.Retrofit;

@Module(includes = {ContextModule.class, ApiModule.class})
public class MainActivityModule {

    @Provides
    public UserService userService(Retrofit retrofit) {
        return retrofit.create(UserService.class);
    }

    @Provides
    public ReferralService referralService(Retrofit retrofit) {
        return retrofit.create(ReferralService.class);
    }
}
