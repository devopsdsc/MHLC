package red.point.checkpoint.ui.wallet;

import dagger.Component;

@Component(modules = WalletModule.class)
public interface WalletComponent {
    void inject(WalletFragment walletFragment);
}
