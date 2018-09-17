package red.point.checkpoint.ui.wallet;

import dagger.Module;
import dagger.Provides;
import red.point.checkpoint.api.service.VoucherService;
import red.point.checkpoint.di.ApiModule;
import red.point.checkpoint.di.ContextModule;
import retrofit2.Retrofit;

@Module(includes = {ContextModule.class, ApiModule.class})
public class AddVoucherModule {

    @Provides
    public VoucherService voucherService(Retrofit retrofit) {
        return retrofit.create(VoucherService.class);
    }
}
