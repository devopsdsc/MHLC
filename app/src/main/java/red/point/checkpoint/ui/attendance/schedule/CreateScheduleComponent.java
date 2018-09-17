package red.point.checkpoint.ui.attendance.schedule;

import dagger.Component;

@Component(modules = CreateScheduleModule.class)
public interface CreateScheduleComponent {
    void inject(CreateScheduleFragment createScheduleFragment);
}
