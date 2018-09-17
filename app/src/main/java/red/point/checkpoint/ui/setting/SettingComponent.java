package red.point.checkpoint.ui.setting;

import dagger.Component;

@Component(modules = SettingModule.class)
public interface SettingComponent {
    void inject(SettingFragment settingFragment);
}
