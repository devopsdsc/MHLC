package red.point.checkpoint.ui.attendance.schedule;

import dagger.Component;

@Component(modules = ShowScheduleModule.class)
public interface ShowScheduleComponent {
    void inject(ShowScheduleFragment showScheduleFragment);
}
