package red.point.checkpoint.ui.registration.setup;

import dagger.Module;
import dagger.Provides;
import red.point.checkpoint.api.service.CompanyService;
import red.point.checkpoint.api.service.EmployeeService;
import red.point.checkpoint.di.ApiModule;
import red.point.checkpoint.di.ContextModule;
import retrofit2.Retrofit;

@Module(includes = {ContextModule.class, ApiModule.class})
public class SetupModule {

    @Provides
    public CompanyService companyService(Retrofit retrofit) {
        return retrofit.create(CompanyService.class);
    }

    @Provides
    public EmployeeService employeeService(Retrofit retrofit) {
        return retrofit.create(EmployeeService.class);
    }
}
