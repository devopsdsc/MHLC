package red.point.checkpoint.ui.attendance.schedule;

import dagger.Component;

@Component(modules = CreateScheduleS2Module.class)
public interface CreateScheduleS2Component {
    void inject(CreateScheduleS2Fragment createScheduleS2Fragment);
}
