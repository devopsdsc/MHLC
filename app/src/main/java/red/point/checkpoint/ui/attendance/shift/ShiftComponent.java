package red.point.checkpoint.ui.attendance.shift;

import dagger.Component;

@Component(modules = ShiftModule.class)
public interface ShiftComponent {
    void inject(ShiftFragment shiftFragment);
    void inject(EditShiftFragment editShiftFragment);
}
