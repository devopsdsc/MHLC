package red.point.checkpoint.ui.attendance.schedule;

import dagger.Component;
import red.point.checkpoint.ui.attendance.attend.MyScheduleFragment;

@Component(modules = ScheduleModule.class)
public interface ScheduleComponent {
    void inject(ScheduleFragment scheduleFragment);
    void inject(MyScheduleFragment myScheduleFragment);
}
