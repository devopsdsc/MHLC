package red.point.checkpoint.ui.account;

import dagger.Component;

@Component(modules = AccountModule.class)
public interface AccountComponent {
    void inject(AccountFragment accountFragment);
}
