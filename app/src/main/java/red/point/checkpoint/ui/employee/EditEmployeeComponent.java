package red.point.checkpoint.ui.employee;

import dagger.Component;

@Component(modules = EditEmployeeModule.class)
public interface EditEmployeeComponent {
    void inject(EditEmployeeFragment editEmployeeFragment);
}
