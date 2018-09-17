package red.point.checkpoint.repository;

import android.arch.lifecycle.MutableLiveData;

import java.util.List;

import red.point.checkpoint.api.model.Branch;
import red.point.checkpoint.api.model.BranchList;
import red.point.checkpoint.api.service.BranchService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BranchRepository {

    private MutableLiveData<List<Branch>> mObservableBranches;

    private BranchService branchService;

    public BranchRepository(BranchService branchService) {
        this.branchService = branchService;
    }

    public MutableLiveData<List<Branch>> getBranches(final long companyId) {
        mObservableBranches = new MutableLiveData<>();

        Call<BranchList> branchListCall = branchService.getBranchList(companyId);

        branchListCall.enqueue(new Callback<BranchList>() {
            @Override
            public void onResponse(Call<BranchList> call, Response<BranchList> response) {
                mObservableBranches.setValue(response.body().getBranches());
            }

            @Override
            public void onFailure(Call<BranchList> call, Throwable t) {
                //
            }
        });

        return mObservableBranches;
    }
}
