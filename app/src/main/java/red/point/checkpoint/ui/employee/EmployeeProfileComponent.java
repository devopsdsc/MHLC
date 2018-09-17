package red.point.checkpoint.ui.employee;

import dagger.Component;

@Component(modules = EmployeeProfileModule.class)
public interface EmployeeProfileComponent {

    void inject(EmployeeProfileFragment employeeProfileFragment);
}
