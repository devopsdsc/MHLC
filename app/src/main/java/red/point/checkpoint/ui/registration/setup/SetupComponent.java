package red.point.checkpoint.ui.registration.setup;

import dagger.Component;

@Component(modules = SetupModule.class)
public interface SetupComponent {
    void injectSetupFragment(SetupFragment setupFragment);
    void injectSetupCompanyFragment(SetupCompanyFragment setupCompanyFragment);
    void injectSetupEmployeeFragment(SetupEmployeeFragment setupEmployeeFragment);
}
