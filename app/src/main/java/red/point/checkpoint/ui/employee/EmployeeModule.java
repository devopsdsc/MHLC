package red.point.checkpoint.ui.employee;

import dagger.Module;
import dagger.Provides;
import red.point.checkpoint.api.service.CompanyService;
import red.point.checkpoint.api.service.EmployeeService;
import red.point.checkpoint.di.ApiModule;
import red.point.checkpoint.di.ContextModule;
import retrofit2.Retrofit;

@Module(includes = {ContextModule.class, ApiModule.class})
public class EmployeeModule {

    @Provides
    public EmployeeService employeeService(Retrofit retrofit) {
        return retrofit.create(EmployeeService.class);
    }

    @Provides
    public CompanyService companyService(Retrofit retrofit) {
        return retrofit.create(CompanyService.class);
    }
}
