package red.point.checkpoint.ui.registration.setup;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import red.point.checkpoint.R;
import red.point.checkpoint.helper.FragmentHelper;
import red.point.checkpoint.ui.registration.SetupActivity;
import red.point.checkpoint.util.ProgressDialogUtil;

public class SetupFragment extends Fragment {

    private Unbinder unbinder;

    public SetupFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_setup, container, false);

        unbinder = ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unbinder.unbind();

        ProgressDialogUtil.dismiss();
    }

    @OnClick(R.id.btn_owner)
    public void setBtnSetupCompany() {
        FragmentHelper.replace(getActivity(), new SetupCompanyFragment(), null, true);
    }

    @OnClick(R.id.btn_employee)
    public void setBtnSetupEmployee() {
        FragmentHelper.replace(getActivity(), new SetupEmployeeFragment(), null, true);
    }

    @OnClick(R.id.btn_sign_out)
    public void setBtnSignOut() {
        ((SetupActivity) getActivity()).logout();
    }
}
