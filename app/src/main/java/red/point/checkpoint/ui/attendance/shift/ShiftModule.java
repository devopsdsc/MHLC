package red.point.checkpoint.ui.attendance.shift;

import dagger.Module;
import dagger.Provides;
import red.point.checkpoint.api.service.ShiftService;
import red.point.checkpoint.di.ApiModule;
import red.point.checkpoint.di.ContextModule;
import retrofit2.Retrofit;

@Module(includes = {ContextModule.class, ApiModule.class})
public class ShiftModule {

    @Provides
    public ShiftService shiftService(Retrofit retrofit) {
        return retrofit.create(ShiftService.class);
    }
}
