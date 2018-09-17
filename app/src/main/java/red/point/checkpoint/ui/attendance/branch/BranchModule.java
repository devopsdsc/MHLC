package red.point.checkpoint.ui.attendance.branch;

import android.content.Context;
import android.os.Bundle;

import java.util.List;

import javax.inject.Inject;

import dagger.Module;
import dagger.Provides;
import red.point.checkpoint.adapter.BranchAdapter;
import red.point.checkpoint.api.model.Branch;
import red.point.checkpoint.api.service.BranchService;
import red.point.checkpoint.di.ApiModule;
import red.point.checkpoint.di.ContextQualifier;
import retrofit2.Retrofit;

@Module(includes = ApiModule.class)
public class BranchModule {
    private Bundle savedInstanceState;
    private List<Branch> listBranch;

    @Inject
    BranchModule(Bundle savedInstanceState, List<Branch> listBranch) {
        this.savedInstanceState = savedInstanceState;
        this.listBranch = listBranch;
    }

    @Provides
    @BranchScope
    public BranchAdapter branchAdapter(@ContextQualifier Context context) {
        return new BranchAdapter(context, savedInstanceState, listBranch);
    }

    @Provides
    @BranchScope
    public BranchService branchService(Retrofit retrofit) {
        return retrofit.create(BranchService.class);
    }
}
