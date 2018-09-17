package red.point.checkpoint.ui.attendance.schedule;

import dagger.Module;
import dagger.Provides;
import red.point.checkpoint.api.service.ScheduleService;
import red.point.checkpoint.di.ApiModule;
import red.point.checkpoint.di.ContextModule;
import retrofit2.Retrofit;

@Module(includes = {ContextModule.class, ApiModule.class})
public class ShowScheduleModule {

    @Provides
    public ScheduleService scheduleService(Retrofit retrofit) {
        return retrofit.create(ScheduleService.class);
    }
}
