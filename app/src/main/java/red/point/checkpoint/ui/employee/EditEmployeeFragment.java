package red.point.checkpoint.ui.employee;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.parceler.Parcels;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import red.point.checkpoint.MyApplication;
import red.point.checkpoint.R;
import red.point.checkpoint.api.model.Employee;
import red.point.checkpoint.api.model.EmployeeResponse;
import red.point.checkpoint.api.service.EmployeeService;
import red.point.checkpoint.di.ContextModule;
import red.point.checkpoint.helper.FragmentHelper;
import red.point.checkpoint.helper.PrefManager;
import red.point.checkpoint.util.ProgressDialogUtil;
import red.point.checkpoint.util.ToastUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class EditEmployeeFragment extends DialogFragment {

    private static final String TAG = EditEmployeeFragment.class.getSimpleName();

    @Inject PrefManager prefManager;
    @Inject EmployeeService employeeService;

    @BindView(R.id.name) EditText mName;
    @BindView(R.id.salary) EditText mSalary;
    @BindView(R.id.hint) TextView mHint;

    private Unbinder unbinder;
    private Call<EmployeeResponse> employeeResponseCall;
    private Employee employee;
    private String name;
    private double salary;
    private View rootView;

    public EditEmployeeFragment() {}

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {

        rootView = getActivity().getLayoutInflater().inflate(R.layout.fragment_edit_employee, null);

        unbinder = ButterKnife.bind(this, rootView);

        EditEmployeeComponent editEmployeeComponent = DaggerEditEmployeeComponent.builder()
                .contextModule(new ContextModule(getContext()))
                .editEmployeeModule(new EditEmployeeModule())
                .build();

        editEmployeeComponent.inject(this);

        employee = Parcels.unwrap(getArguments().getParcelable("employee"));

        prefManager = new PrefManager(MyApplication.getAppContext());

        mName.setText(employee.getName());

        mSalary.setText(String.format("%s", employee.getSalary()));
        if (employee.getSalaryType().equals("Monthly")) {
            mHint.setText("* salary / month");
        } else {
            mHint.setText("* salary / day");
        }

        return editDialog();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();

        ProgressDialogUtil.dismiss();
    }

    private AlertDialog editDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Edit employee");
        builder.setPositiveButton("Save", null);
        builder.setNegativeButton("Cancel", (dialog, which) -> dismiss());

        builder.setView(rootView);
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialog1 -> {
            Button positiveButton = ((AlertDialog) dialog1).getButton(DialogInterface.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                if (TextUtils.isEmpty(mName.getText())) {
                    ToastUtil.show("Name cannot be empty");
                    return;
                }

                if (TextUtils.isEmpty(mSalary.getText())) {
                    ToastUtil.show("Salary cannot be empty");
                    return;
                }

                name = mName.getText().toString().trim();
                salary = Double.parseDouble(mSalary.getText().toString().trim());

                ProgressDialogUtil.showLoading(getContext());
                employeeResponseCall = employeeService.putEmployee(prefManager.getCompanyId(),
                        employee.getId(),
                        name,
                        employee.getSalaryType(),
                        salary,
                        employee.getAdmin(),
                        employee.getOwner());

                employeeResponseCall.enqueue(new Callback<EmployeeResponse>() {
                    @Override
                    public void onResponse(Call<EmployeeResponse> call, Response<EmployeeResponse> response) {
                        ProgressDialogUtil.dismiss();
                        if (response.isSuccessful()) {
                            ToastUtil.show("Employee Updated");
                            dismiss();

                            Bundle bundle = new Bundle();
                            bundle.putParcelable("employee", Parcels.wrap(response.body().getEmployee()));

                            FragmentHelper.replace(getActivity(), new EmployeeProfileFragment(), bundle, false);
                        }
                    }

                    @Override
                    public void onFailure(Call<EmployeeResponse> call, Throwable t) {
                        if (! call.isCanceled()) {
                            ProgressDialogUtil.dismiss();
                            ToastUtil.show("Connection Failure");
                            dismiss();
                        }
                    }
                });
            });
        });

        return dialog;
    }
}
