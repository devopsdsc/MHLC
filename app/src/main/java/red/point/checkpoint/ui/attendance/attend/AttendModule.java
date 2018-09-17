package red.point.checkpoint.ui.attendance.attend;

import dagger.Module;
import dagger.Provides;
import red.point.checkpoint.api.service.ScheduleService;
import red.point.checkpoint.di.ApiModule;
import red.point.checkpoint.di.ContextModule;
import retrofit2.Retrofit;

@Module(includes = {ContextModule.class, ApiModule.class})
public class AttendModule {

    @Provides
    public ScheduleService scheduleService(Retrofit retrofit) {
        return retrofit.create(ScheduleService.class);
    }
}
