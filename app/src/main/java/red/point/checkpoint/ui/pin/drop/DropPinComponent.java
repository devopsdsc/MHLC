package red.point.checkpoint.ui.pin.drop;

import dagger.Component;

@Component(modules = DropPinModule.class)
public interface DropPinComponent {
    void inject(DropPinFragment dropPinFragment);
    void injectTagLocation(TagLocationFragment tagLocationFragment);
}
