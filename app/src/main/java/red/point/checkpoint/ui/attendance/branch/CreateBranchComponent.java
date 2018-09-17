package red.point.checkpoint.ui.attendance.branch;

import dagger.Component;

@Component(modules = CreateBranchModule.class)
public interface CreateBranchComponent {
    void inject(CreateBranchFragment createBranchFragment);
}
