package red.point.checkpoint.ui.wallet;

import dagger.Component;

@Component(modules = AddVoucherModule.class)
public interface AddVoucherComponent {
    void inject(AddVoucherFragment addVoucherFragment);
}
