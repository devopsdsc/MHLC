package red.point.checkpoint.ui.attendance.attend;

import dagger.Component;

@Component(modules = AttendModule.class)
public interface AttendComponent {
    void inject(AttendFragment attendFragment);
}
