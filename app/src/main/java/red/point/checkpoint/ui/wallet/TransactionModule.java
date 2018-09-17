package red.point.checkpoint.ui.wallet;

import dagger.Module;
import dagger.Provides;
import red.point.checkpoint.api.service.CompanyService;
import red.point.checkpoint.api.service.TransactionService;
import red.point.checkpoint.di.ApiModule;
import red.point.checkpoint.di.ContextModule;
import retrofit2.Retrofit;

@Module(includes = {ContextModule.class, ApiModule.class})
public class TransactionModule {

    @Provides
    public CompanyService companyService(Retrofit retrofit) {
        return retrofit.create(CompanyService.class);
    }

    @Provides
    public TransactionService transactionService(Retrofit retrofit) {
        return retrofit.create(TransactionService.class);
    }
}
