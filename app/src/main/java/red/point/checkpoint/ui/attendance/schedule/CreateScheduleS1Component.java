package red.point.checkpoint.ui.attendance.schedule;

import dagger.Component;

@Component(modules = CreateScheduleS1Module.class)
public interface CreateScheduleS1Component {
    void inject(CreateScheduleS1Fragment createScheduleS1Fragment);
}
