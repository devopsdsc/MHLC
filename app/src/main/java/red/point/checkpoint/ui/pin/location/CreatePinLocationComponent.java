package red.point.checkpoint.ui.pin.location;

import dagger.Component;

@Component(modules = CreatePinLocationModule.class)
public interface CreatePinLocationComponent {
    void inject(CreatePinLocationFragment createPinLocationFragment);
}
