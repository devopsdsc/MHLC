package red.point.checkpoint.ui.attendance.branch;

import dagger.Module;
import dagger.Provides;
import red.point.checkpoint.api.service.BranchService;
import red.point.checkpoint.di.ApiModule;
import red.point.checkpoint.di.ContextModule;
import retrofit2.Retrofit;

@Module(includes = {ContextModule.class, ApiModule.class})
public class BranchSingleModule {
    @Provides
    public BranchService branchService(Retrofit retrofit) {
        return retrofit.create(BranchService.class);
    }
}
