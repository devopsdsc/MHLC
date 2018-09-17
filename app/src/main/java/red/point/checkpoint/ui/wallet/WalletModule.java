package red.point.checkpoint.ui.wallet;

import dagger.Module;
import dagger.Provides;
import red.point.checkpoint.api.service.CompanyService;
import red.point.checkpoint.api.service.TopUpService;
import red.point.checkpoint.di.ApiModule;
import red.point.checkpoint.di.ContextModule;
import retrofit2.Retrofit;

@Module(includes = {ContextModule.class, ApiModule.class})
public class WalletModule {

    @Provides
    public CompanyService companyService(Retrofit retrofit) {
        return retrofit.create(CompanyService.class);
    }

    @Provides
    public TopUpService topUpService(Retrofit retrofit) {
        return retrofit.create(TopUpService.class);
    }
}
