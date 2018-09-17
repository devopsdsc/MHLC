package red.point.checkpoint.ui.employee;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.parceler.Parcels;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import red.point.checkpoint.R;
import red.point.checkpoint.api.model.Employee;
import red.point.checkpoint.api.model.EmployeeResponse;
import red.point.checkpoint.api.service.EmployeeService;
import red.point.checkpoint.di.ContextModule;
import red.point.checkpoint.helper.FragmentHelper;
import red.point.checkpoint.helper.PrefManager;
import red.point.checkpoint.ui.MainFragment;
import red.point.checkpoint.util.NumberUtil;
import red.point.checkpoint.util.ProgressDialogUtil;
import red.point.checkpoint.util.ToastUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployeeProfileFragment extends MainFragment {

    private static final String TAG = EmployeeProfileFragment.class.getSimpleName();

    @Inject PrefManager prefManager;
    @Inject EmployeeService employeeService;
    @Inject EmployeeService deleteEmployeeService;
    @Inject EmployeeService promoteEmployeeService;
    @Inject EmployeeService revokeEmployeeService;

    @BindView(R.id.btn_delete) LinearLayout mBtnDelete;
    @BindView(R.id.btn_promote_admin) LinearLayout mBtnPromote;
    @BindView(R.id.name) TextView mName;
    @BindView(R.id.email) TextView mEmail;
    @BindView(R.id.base_salary) TextView mSalary;
    @BindView(R.id.salary_type) TextView mSalaryType;
    @BindView(R.id.promote_admin) TextView mPromote;

    private Unbinder unbinder;
    private Call<EmployeeResponse> employeeResponseCall;
    private Call<EmployeeResponse> deleteEmployeeResponseCall;
    private Call<EmployeeResponse> promoteEmployeeResponseCall;
    private Call<EmployeeResponse> revokeEmployeeResponseCall;
    private Employee employee;

    public EmployeeProfileFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (getActivity() != null) getActivity().setTitle(R.string.nav_employee);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_employee_profile, container, false);

        unbinder = ButterKnife.bind(this, rootView);

        EmployeeProfileComponent employeeProfileComponent = DaggerEmployeeProfileComponent.builder()
                .contextModule(new ContextModule(getContext()))
                .employeeProfileModule(new EmployeeProfileModule())
                .build();

        employeeProfileComponent.inject(this);

        loadData();

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();

        if (employeeResponseCall != null && employeeResponseCall.isExecuted()) employeeResponseCall.cancel();
        if (promoteEmployeeResponseCall != null && promoteEmployeeResponseCall.isExecuted()) promoteEmployeeResponseCall.cancel();
        if (revokeEmployeeResponseCall != null && revokeEmployeeResponseCall.isExecuted()) revokeEmployeeResponseCall.cancel();
        if (deleteEmployeeResponseCall != null && deleteEmployeeResponseCall.isExecuted()) deleteEmployeeResponseCall.cancel();

        ProgressDialogUtil.dismiss();
    }

    @OnClick(R.id.btn_salary_type)
    public void editSalaryType() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());
        builderSingle.setIcon(R.drawable.ic_salary);
        builderSingle.setTitle("CHOOSE SALARY TYPE");

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.select_dialog_singlechoice);

        arrayAdapter.add("Monthly");
        arrayAdapter.add("Daily");

        builderSingle.setNegativeButton("cancel", (dialog, which) -> dialog.dismiss());
        builderSingle.setAdapter(arrayAdapter, (dialog, which) -> {
            String salaryType = arrayAdapter.getItem(which);
            mSalaryType.setText(String.format("%s Salary", salaryType));

            ProgressDialogUtil.showLoading(getContext());
            employeeResponseCall = employeeService.putEmployee(prefManager.getCompanyId(),
                    employee.getId(),
                    employee.getName(),
                    salaryType,
                    employee.getSalary(),
                    employee.getAdmin(),
                    employee.getOwner());

            employeeResponseCall.enqueue(new Callback<EmployeeResponse>() {
                @Override
                public void onResponse(Call<EmployeeResponse> call, Response<EmployeeResponse> response) {
                    ProgressDialogUtil.dismiss();
                    if (response.isSuccessful()) {
                        employee = response.body().getEmployee();
                        ToastUtil.show("Employee Updated");
                    }
                }

                @Override
                public void onFailure(Call<EmployeeResponse> call, Throwable t) {
                    if (! call.isCanceled()) {
                        ProgressDialogUtil.dismiss();
                        ToastUtil.show("Connection Failure");
                    }
                }
            });
        });
        builderSingle.show();
    }

    @OnClick(R.id.btn_edit)
    public void edit() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("employee", Parcels.wrap(employee));

        FragmentHelper.show(getActivity(), new EditEmployeeFragment(), bundle);
    }

    @OnClick(R.id.btn_delete)
    public void deleteEmployee() {
        if (employee.getOwner()) {
            isOwnerDialog();
        } else {
            new AlertDialog.Builder(getActivity())
                    .setIcon(R.drawable.ic_warning_24dp)
                    .setTitle("Remove employee")
                    .setMessage(R.string.remove_user_notification)
                    .setPositiveButton("Remove", (dialog, whichButton) -> {
                        ProgressDialogUtil.showLoading(getContext());
                        deleteEmployeeResponseCall = deleteEmployeeService.deleteEmployee(prefManager.getCompanyId(), employee.getId());
                        deleteEmployeeResponseCall.enqueue(new Callback<EmployeeResponse>() {
                            @Override
                            public void onResponse(Call<EmployeeResponse> call, Response<EmployeeResponse> response) {
                                ProgressDialogUtil.dismiss();
                                if (response.isSuccessful()) {
                                    ToastUtil.show("Employee removed");
                                }
                            }

                            @Override
                            public void onFailure(Call<EmployeeResponse> call, Throwable t) {
                                if (! call.isCanceled()) {
                                    ProgressDialogUtil.dismiss();
                                    ToastUtil.show("Connection Failure");
                                }
                            }
                        });
                    })
                    .setNegativeButton("Cancel", (dialog, whichButton) -> {})
                    .show();
        }
    }

    @OnClick(R.id.btn_promote_admin)
    public void promote() {
        if (employee.getOwner()) {
            isOwnerDialog();
        } else if (employee.getAdmin()) {
            new AlertDialog.Builder(getActivity())
                    .setIcon(R.drawable.ic_delete)
                    .setTitle("Revoke admin")
                    .setMessage("Revoked user will loss access to manage employee schedule, salary, reward, and late charge")
                    .setPositiveButton("Revoke access", (dialog, whichButton) -> {
                        ProgressDialogUtil.showLoading(getContext());
                        revokeEmployeeResponseCall = revokeEmployeeService.putEmployee(prefManager.getCompanyId(),
                                employee.getId(),
                                employee.getName(),
                                employee.getSalaryType(),
                                employee.getSalary(),
                                false,
                                employee.getOwner());

                        revokeEmployeeResponseCall.enqueue(new Callback<EmployeeResponse>() {
                            @Override
                            public void onResponse(Call<EmployeeResponse> call, Response<EmployeeResponse> response) {
                                ProgressDialogUtil.dismiss();
                                if (response.isSuccessful()) {
                                    employee = response.body().getEmployee();
                                    mPromote.setText(R.string.promote_admin);
                                    mName.setText(String.format("%s %s", employee.getName(), getStatus()));
                                    ToastUtil.show(employee.getName() + " revoked");
                                }
                            }

                            @Override
                            public void onFailure(Call<EmployeeResponse> call, Throwable t) {
                                if (! call.isCanceled()) {
                                    ProgressDialogUtil.dismiss();
                                    ToastUtil.show("Connection Failure");
                                }
                            }
                        });
                    })
                    .setNegativeButton("Cancel", (dialog, whichButton) -> {})
                    .show();
        } else {
            new AlertDialog.Builder(getActivity())
                    .setIcon(R.drawable.ic_medal)
                    .setTitle("Promote admin")
                    .setMessage("Promoted admin can manage employee schedule, salary, reward, and late charge")
                    .setPositiveButton("Promote Admin", (dialog, whichButton) -> {
                        ProgressDialogUtil.showLoading(getContext());
                        promoteEmployeeResponseCall = promoteEmployeeService.putEmployee(prefManager.getCompanyId(),
                                employee.getId(),
                                employee.getName(),
                                employee.getSalaryType(),
                                employee.getSalary(),
                                true,
                                employee.getOwner());

                        promoteEmployeeResponseCall.enqueue(new Callback<EmployeeResponse>() {
                            @Override
                            public void onResponse(Call<EmployeeResponse> call, Response<EmployeeResponse> response) {
                                ProgressDialogUtil.dismiss();
                                if (response.isSuccessful()) {
                                    employee = response.body().getEmployee();
                                    mPromote.setText(R.string.revoke_admin);
                                    mName.setText(String.format("%s %s", employee.getName(), getStatus()));
                                    ToastUtil.show(employee.getName() + " promoted");
                                }
                            }

                            @Override
                            public void onFailure(Call<EmployeeResponse> call, Throwable t) {
                                if (! call.isCanceled()) {
                                    ProgressDialogUtil.dismiss();
                                    ToastUtil.show("Connection Failure");
                                }
                            }
                        });
                    })
                    .setNegativeButton("Cancel", (dialog, whichButton) -> {})
                    .show();
        }

    }

    private void loadData() {
        employee = Parcels.unwrap(getArguments().getParcelable("employee"));

        mName.setText(String.format("%s %s", employee.getName(), getStatus()));
        mEmail.setText(employee.getEmail());
        mSalary.setText(NumberUtil.getFormattedNumber(employee.getSalary()));
        mSalaryType.setText(String.format("%s Salary", employee.getSalaryType()));
        mPromote.setText("-");

        if (employee.getAdmin()) {
            mPromote.setText(R.string.revoke_admin);
        } else {
            mPromote.setText(R.string.promote_admin);
        }

        if (employee.getOwner()) {
            mBtnDelete.setVisibility(View.GONE);
            mBtnPromote.setVisibility(View.GONE);
        }
    }

    private String getStatus() {
        if (employee.getOwner()) {
            return "(Owner)";
        }

        if (employee.getAdmin()) {
            return "(Admin)";
        }

        return "";
    }

    private void isOwnerDialog() {
        new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.ic_warning_24dp)
                .setTitle("Master User")
                .setMessage("You cannot promote, revoke access or remove user master")
                .setPositiveButton("Close", (dialog, whichButton) -> {})
                .show();
    }
}
