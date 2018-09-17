package red.point.checkpoint.ui.registration.referral;

import dagger.Module;
import dagger.Provides;
import red.point.checkpoint.api.service.ReferralService;
import red.point.checkpoint.api.service.ReferralSourceService;
import red.point.checkpoint.di.ApiModule;
import red.point.checkpoint.di.ContextModule;
import retrofit2.Retrofit;

@Module(includes = {ContextModule.class, ApiModule.class})
public class ReferralModule {

    @Provides
    public ReferralSourceService referralSourceService(Retrofit retrofit) {
        return retrofit.create(ReferralSourceService.class);
    }

    @Provides
    public ReferralService referralService(Retrofit retrofit) {
        return retrofit.create(ReferralService.class);
    }
}
