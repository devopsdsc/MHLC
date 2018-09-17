package red.point.checkpoint.ui.attendance.branch;

import dagger.Component;

@Component(modules = EditBranchModule.class)
public interface EditBranchComponent {
    void inject(EditBranchFragment editBranchFragment);
}
