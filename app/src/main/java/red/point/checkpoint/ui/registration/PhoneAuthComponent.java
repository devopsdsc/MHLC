package red.point.checkpoint.ui.registration;

import dagger.Component;

@Component(modules = PhoneAuthModule.class)
public interface PhoneAuthComponent {
    void inject(PhoneAuthActivity phoneAuthActivity);
}
