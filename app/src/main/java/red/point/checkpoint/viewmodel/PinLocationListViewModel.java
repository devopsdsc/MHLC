package red.point.checkpoint.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import java.util.List;

import red.point.checkpoint.api.model.PinLocation;
import red.point.checkpoint.repository.PinLocationRepository;

public class PinLocationListViewModel extends AndroidViewModel{

    private MutableLiveData<List<PinLocation>> mMutableLiveDataPinLocations;

    private final long companyId;

    private PinLocationRepository pinLocationRepository;

    PinLocationListViewModel(Application application, PinLocationRepository repository, long companyId) {
        super(application);

        this.pinLocationRepository = repository;
        this.companyId = companyId;

        mMutableLiveDataPinLocations = pinLocationRepository.getPinLocations(companyId);
    }

    /**
     * Expose the LiveData Products query so the UI can observe it.
     */
    public LiveData<List<PinLocation>> getPinLocations() {
        return mMutableLiveDataPinLocations;
    }

    /**
     * A creator is used to inject the product ID into the ViewModel
     * <p>
     * This creator is to showcase how to inject dependencies into ViewModels. It's not
     * actually necessary in this case, as the product ID can be passed in a public method.
     */
    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        @NonNull
        private final Application mApplication;
        private final PinLocationRepository mRepository;
        private final long mCompanyId;

        public Factory(@NonNull Application application, PinLocationRepository repository, long companyId) {
            mApplication = application;
            mCompanyId = companyId;
            mRepository = repository;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            //noinspection unchecked
            return (T) new PinLocationListViewModel(mApplication, mRepository, mCompanyId);
        }
    }
}
