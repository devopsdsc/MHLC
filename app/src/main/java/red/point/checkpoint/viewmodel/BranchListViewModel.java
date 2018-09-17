package red.point.checkpoint.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import java.util.List;

import red.point.checkpoint.api.model.Branch;
import red.point.checkpoint.repository.BranchRepository;

public class BranchListViewModel extends AndroidViewModel{

    private MutableLiveData<List<Branch>> mMutableLiveDataBranches;

    private final long companyId;

    private BranchRepository branchRepository;

    BranchListViewModel(Application application, BranchRepository repository, long companyId) {
        super(application);

        this.branchRepository = repository;
        this.companyId = companyId;

        mMutableLiveDataBranches = branchRepository.getBranches(companyId);
    }

    /**
     * Expose the LiveData Products query so the UI can observe it.
     */
    public LiveData<List<Branch>> getBranches() {
        return mMutableLiveDataBranches;
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
        private final BranchRepository mRepository;
        private final long mCompanyId;

        public Factory(@NonNull Application application, BranchRepository repository, long companyId) {
            mApplication = application;
            mCompanyId = companyId;
            mRepository = repository;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            //noinspection unchecked
            return (T) new BranchListViewModel(mApplication, mRepository, mCompanyId);
        }
    }
}
