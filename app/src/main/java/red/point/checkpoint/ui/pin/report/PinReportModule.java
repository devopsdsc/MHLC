package red.point.checkpoint.ui.pin.report;

import dagger.Module;
import dagger.Provides;
import red.point.checkpoint.api.service.EmployeeService;
import red.point.checkpoint.api.service.PinService;
import red.point.checkpoint.di.ApiModule;
import red.point.checkpoint.di.ContextModule;
import retrofit2.Retrofit;

@Module(includes = {ContextModule.class, ApiModule.class})
public class PinReportModule {

    @Provides
    public EmployeeService employeeService(Retrofit retrofit) {
        return retrofit.create(EmployeeService.class);
    }

    @Provides
    public PinService pinService(Retrofit retrofit) {
        return retrofit.create(PinService.class);
    }
}
