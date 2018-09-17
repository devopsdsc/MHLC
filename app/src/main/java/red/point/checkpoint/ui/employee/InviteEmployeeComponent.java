package red.point.checkpoint.ui.employee;

import dagger.Component;

@Component(modules = InviteEmployeeModule.class)
public interface InviteEmployeeComponent {
    void inject(InviteEmployeeFragment inviteEmployeeFragment);
}
