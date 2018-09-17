package red.point.checkpoint.ui.pin.report;

import dagger.Component;
import red.point.checkpoint.ui.pin.drop.MyPinReportFragment;

@Component(modules = PinReportModule.class)
public interface PinReportComponent {

    void inject(MyPinReportFragment myPinReportFragment);
    void injectPinReport(PinReportFragment pinReportFragment);
    void injectPinDailyReport(PinDailyReportFragment pinDailyReportFragment);
}
