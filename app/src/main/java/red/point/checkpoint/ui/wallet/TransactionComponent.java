package red.point.checkpoint.ui.wallet;

import dagger.Component;

@Component(modules = TransactionModule.class)
public interface TransactionComponent {
    void inject(TransactionFragment transactionFragment);
}
