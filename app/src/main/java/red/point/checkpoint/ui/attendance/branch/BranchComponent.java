package red.point.checkpoint.ui.attendance.branch;

import dagger.Component;
import red.point.checkpoint.di.ContextModule;

@Component(modules = {
        BranchModule.class,
        ContextModule.class
})
@BranchScope
public interface BranchComponent {
    void inject(BranchFragment branchFragment);
}
