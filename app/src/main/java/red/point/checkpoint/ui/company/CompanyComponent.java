package red.point.checkpoint.ui.company;

import dagger.Component;

@Component(modules = CompanyModule.class)
public interface CompanyComponent {
    void inject(CompanyFragment companyFragment);
    void inject(EditCompanyNameFragment editCompanyNameFragment);
}
