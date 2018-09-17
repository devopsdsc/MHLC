package red.point.checkpoint.ui.pin.location;

import java.util.List;

import javax.inject.Inject;

import dagger.Module;
import dagger.Provides;
import red.point.checkpoint.adapter.PinLocationAdapter;
import red.point.checkpoint.api.model.PinLocation;
import red.point.checkpoint.api.service.PinLocationService;
import red.point.checkpoint.di.ApiModule;
import retrofit2.Retrofit;

@Module(includes = ApiModule.class)
public class PinLocationModule {
    private List<PinLocation> listPinLocation;

    @Inject
    PinLocationModule(List<PinLocation> listPinLocation) {
        this.listPinLocation = listPinLocation;
    }

    @Provides
    public PinLocationAdapter pinLocationAdapter() {
        return new PinLocationAdapter(listPinLocation);
    }

    @Provides
    public PinLocationService branchService(Retrofit retrofit) {
        return retrofit.create(PinLocationService.class);
    }
}
