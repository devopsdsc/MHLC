package red.point.checkpoint.ui.attendance.branch;

import dagger.Component;

@Component(modules = BranchScheduleModule.class)
public interface BranchScheduleComponent {
    void inject(BranchScheduleFragment branchScheduleFragment);
}
