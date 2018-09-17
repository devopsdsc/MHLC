package red.point.checkpoint.ui.attendance.branch;

import dagger.Component;

@Component(modules = BranchSingleModule.class)
public interface BranchSingleComponent {
    void inject(BranchSingleFragment branchSingleFragment);
}
