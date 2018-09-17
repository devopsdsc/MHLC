package red.point.checkpoint.ui.registration.referral;

import dagger.Component;

@Component(modules = ReferralModule.class)
public interface ReferralComponent {
    void inject(ReferralFragment referralFragment);
    void injectDialog(ReferralDialogFragment referralDialogFragment);
}
