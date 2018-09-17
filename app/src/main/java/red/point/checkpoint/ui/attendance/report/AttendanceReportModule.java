package red.point.checkpoint.ui.attendance.report;

import dagger.Module;
import dagger.Provides;
import red.point.checkpoint.api.service.EmployeeService;
import red.point.checkpoint.api.service.ScheduleService;
import red.point.checkpoint.di.ApiModule;
import red.point.checkpoint.di.ContextModule;
import retrofit2.Retrofit;

@Module(includes = {ContextModule.class, ApiModule.class})
public class AttendanceReportModule {

    @Provides
    public ScheduleService scheduleService(Retrofit retrofit) {
        return retrofit.create(ScheduleService.class);
    }

    @Provides
    public EmployeeService employeeService(Retrofit retrofit) {
        return retrofit.create(EmployeeService.class);
    }
}
