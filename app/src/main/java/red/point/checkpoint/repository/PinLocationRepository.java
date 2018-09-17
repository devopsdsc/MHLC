package red.point.checkpoint.repository;

import android.arch.lifecycle.MutableLiveData;

import java.util.List;

import red.point.checkpoint.api.model.PinLocation;
import red.point.checkpoint.api.model.PinLocationList;
import red.point.checkpoint.api.service.PinLocationService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PinLocationRepository {

    private MutableLiveData<List<PinLocation>> mObservablePinLocations;

    private PinLocationService branchService;

    public PinLocationRepository(PinLocationService branchService) {
        this.branchService = branchService;
    }

    public MutableLiveData<List<PinLocation>> getPinLocations(final long companyId) {
        mObservablePinLocations = new MutableLiveData<>();

        Call<PinLocationList> branchListCall = branchService.getPinLocationList(companyId, 50, -7.2914248,112.6726697, 100);

        branchListCall.enqueue(new Callback<PinLocationList>() {
            @Override
            public void onResponse(Call<PinLocationList> call, Response<PinLocationList> response) {
                mObservablePinLocations.setValue(response.body().getPinLocations());
            }

            @Override
            public void onFailure(Call<PinLocationList> call, Throwable t) {
                //
            }
        });

        return mObservablePinLocations;
    }
}
