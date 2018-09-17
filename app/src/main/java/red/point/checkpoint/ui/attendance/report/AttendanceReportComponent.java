package red.point.checkpoint.ui.attendance.report;

import dagger.Component;

@Component(modules = AttendanceReportModule.class)
public interface AttendanceReportComponent {
    void inject(AttendanceReportFragment attendanceReportFragment);
}
