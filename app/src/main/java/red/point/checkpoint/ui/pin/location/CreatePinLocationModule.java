package red.point.checkpoint.ui.pin.location;

import dagger.Module;
import dagger.Provides;
import red.point.checkpoint.api.service.PinLocationService;
import red.point.checkpoint.di.ApiModule;
import red.point.checkpoint.di.ContextModule;
import retrofit2.Retrofit;

@Module(includes = {ContextModule.class, ApiModule.class})
public class CreatePinLocationModule {
    @Provides
    public PinLocationService branchService(Retrofit retrofit) {
        return retrofit.create(PinLocationService.class);
    }
}
