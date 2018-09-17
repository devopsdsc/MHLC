package red.point.checkpoint.ui.pin.drop;

import dagger.Module;
import dagger.Provides;
import red.point.checkpoint.api.service.PinLocationService;
import red.point.checkpoint.api.service.PinService;
import red.point.checkpoint.di.ApiModule;
import red.point.checkpoint.di.ContextModule;
import retrofit2.Retrofit;

@Module(includes = {ContextModule.class, ApiModule.class})
public class DropPinModule {

    @Provides
    public PinService pinService(Retrofit retrofit) {
        return retrofit.create(PinService.class);
    }

    @Provides
    public PinLocationService pinLocationService(Retrofit retrofit) {
        return retrofit.create(PinLocationService.class);
    }
}
