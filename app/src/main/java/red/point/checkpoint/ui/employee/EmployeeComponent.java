package red.point.checkpoint.ui.employee;

import dagger.Component;

@Component(modules = EmployeeModule.class)
public interface EmployeeComponent {

    void inject(EmployeeFragment employeeFragment);
}
