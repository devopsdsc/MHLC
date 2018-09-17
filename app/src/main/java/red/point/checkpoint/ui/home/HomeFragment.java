package red.point.checkpoint.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import red.point.checkpoint.R;
import red.point.checkpoint.api.model.EmployeeResponse;
import red.point.checkpoint.api.service.EmployeeService;
import red.point.checkpoint.di.ContextModule;
import red.point.checkpoint.helper.FragmentHelper;
import red.point.checkpoint.helper.PrefManager;
import red.point.checkpoint.ui.attendance.attend.AttendFragment;
import red.point.checkpoint.ui.attendance.menu.AttendanceMenuFragment;
import red.point.checkpoint.ui.pin.drop.DropPinFragment;
import red.point.checkpoint.ui.pin.menu.DropPinMenuFragment;
import red.point.checkpoint.util.ProgressDialogUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private static final String TAG = HomeFragment.class.getSimpleName();

    private Unbinder unbinder;

    @Inject
    PrefManager prefManager;

    @Inject
    EmployeeService employeeService;

    private Call<EmployeeResponse> employeeResponseCall;
    private Boolean isAdmin = null;
    private Boolean isClickMenuAttendance = null;
    private Boolean isClickMenuDropPin = null;

    public HomeFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        unbinder = ButterKnife.bind(this, rootView);

        HomeComponent homeComponent = DaggerHomeComponent.builder()
                .contextModule(new ContextModule(getContext()))
                .build();

        homeComponent.inject(this);

        if (getActivity() != null) getActivity().setTitle(prefManager.getCompanyName());

        employeeResponseCall = employeeService.getEmployee(prefManager.getCompanyId(), prefManager.getUserId());
        employeeResponseCall.enqueue(new Callback<EmployeeResponse>() {
            @Override
            public void onResponse(Call<EmployeeResponse> call, Response<EmployeeResponse> response) {
                if (response.isSuccessful()) {
                    if (response.body().getEmployee().getAdmin() || response.body().getEmployee().getOwner()) {
                        isAdmin = true;

                        if (isClickMenuAttendance != null && isClickMenuAttendance) menuAttendance();
                        if (isClickMenuDropPin != null && isClickMenuDropPin) menuDropPin();
                    } else {
                        isAdmin = false;

                        if (isClickMenuAttendance != null && isClickMenuAttendance
                                || isClickMenuDropPin != null && isClickMenuDropPin) {
                            isNotAdminDialog();
                        }
                    }
                }

                ProgressDialogUtil.dismiss();
            }

            @Override
            public void onFailure(Call<EmployeeResponse> call, Throwable t) {
                if (!call.isCanceled()) {
                    ProgressDialogUtil.dismiss();
                }
            }
        });

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (employeeResponseCall != null && employeeResponseCall.isExecuted()) employeeResponseCall.cancel();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null) unbinder.unbind();
        ProgressDialogUtil.dismiss();
    }

    @OnClick(R.id.attend_attendance)
    public void attendAttendance() {
        FragmentHelper.replace(getActivity(), new AttendFragment(), null, true);
    }

    @OnClick(R.id.menu_attendance)
    public void menuAttendance() {
        isClickMenuAttendance = true;

        if (isAdmin == null) {
            ProgressDialogUtil.showLoading(getContext());
            return;
        }

        if (!isAdmin) {
            isNotAdminDialog();
            return;
        }

        isClickMenuAttendance = false;

        FragmentHelper.replace(getActivity(), new AttendanceMenuFragment(), null, true);
    }

    @OnClick(R.id.drop_pin)
    public void dropPin() {
        FragmentHelper.replace(getActivity(), new DropPinFragment(), null, true);
    }

    @OnClick(R.id.menu_drop_pin)
    public void menuDropPin() {
        isClickMenuDropPin = true;

        if (isAdmin == null) {
            ProgressDialogUtil.showLoading(getContext());
            return;
        }

        if (!isAdmin) {
            isNotAdminDialog();
            return;
        }

        isClickMenuDropPin = false;

        FragmentHelper.replace(getActivity(), new DropPinMenuFragment(), null, true);
    }

    private void isNotAdminDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle("Restricted Access")
                .setMessage("Only admin can access this menu")
                .setPositiveButton("Close", (dialog, whichButton) -> {})
                .show();
    }
}
