package red.point.checkpoint.ui.pin.location;

import dagger.Component;
import red.point.checkpoint.di.ContextModule;
import red.point.checkpoint.ui.attendance.branch.BranchScope;

@Component(modules = {
        PinLocationModule.class,
        ContextModule.class
})
@BranchScope
public interface PinLocationComponent {
    void inject(PinLocationFragment pinLocationFragment);
}
