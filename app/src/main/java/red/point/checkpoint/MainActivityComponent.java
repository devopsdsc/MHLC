package red.point.checkpoint;

import dagger.Component;
import red.point.checkpoint.ui.MainActivity;

@Component(modules = MainActivityModule.class)
public interface MainActivityComponent {

    void inject(MainActivity mainActivity);
}
