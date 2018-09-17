package red.point.checkpoint.ui.setting;

import dagger.Component;

@Component(modules = EditSettingModule.class)
public interface EditSettingComponent {

    void injectEditCheckInRadius(EditCheckInRadiusFragment editCheckInRadiusFragment);
    void injectEditReward(EditRewardFragment editRewardFragment);
    void injectEditLateCharge(EditLateChargeFragment editLateChargeFragment);
    void injectEditLateRange(EditLateRangeFragment editLateRangeFragment);
    void injectEditMaxCharge(EditMaxChargeFragment editMaxChargeFragment);
}
