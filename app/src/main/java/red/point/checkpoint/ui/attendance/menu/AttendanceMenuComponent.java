package red.point.checkpoint.ui.attendance.menu;

import dagger.Component;

@Component(modules = AttendanceMenuModule.class)
public interface AttendanceMenuComponent {
    void inject(AttendanceMenuFragment attendanceMenuFragment);
}
