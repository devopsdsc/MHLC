package red.point.checkpoint.ui.notification;

import dagger.Component;

@Component(modules = NotificationModule.class)
public interface NotificationComponent {
    void inject(NotificationFragment notificationFragment);
}
