package red.point.checkpoint.ui.home;

import dagger.Component;

@Component(modules = HomeModule.class)
public interface HomeComponent {
    void inject(HomeFragment homeFragment);
}
