package red.point.checkpoint.ui.pin.location;

import dagger.Component;

@Component(modules = EditPinLocationModule.class)
public interface EditPinLocationComponent {
    void inject(EditPinLocationFragment editPinLocationFragment);
}
