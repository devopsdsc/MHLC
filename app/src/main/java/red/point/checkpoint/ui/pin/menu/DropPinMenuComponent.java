package red.point.checkpoint.ui.pin.menu;

import dagger.Component;

@Component(modules = DropPinMenuModule.class)
public interface DropPinMenuComponent {
    void inject(DropPinMenuFragment dropPinMenuFragment);
}
