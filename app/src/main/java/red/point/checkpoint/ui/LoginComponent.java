package red.point.checkpoint.ui;

import dagger.Component;

@Component(modules = LoginModule.class)
public interface LoginComponent {
    void inject(LoginActivity loginActivity);
}