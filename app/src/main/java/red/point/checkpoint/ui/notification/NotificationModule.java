package red.point.checkpoint.ui.notification;

import dagger.Module;
import dagger.Provides;
import red.point.checkpoint.api.service.EmployeeService;
import red.point.checkpoint.api.service.NotificationService;
import red.point.checkpoint.di.ApiModule;
import red.point.checkpoint.di.ContextModule;
import retrofit2.Retrofit;

@Module(includes = {ContextModule.class, ApiModule.class})
public class NotificationModule {

    @Provides
    public NotificationService notificationService(Retrofit retrofit) {
        return retrofit.create(NotificationService.class);
    }
}
