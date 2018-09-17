package red.point.checkpoint.ui.company;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import red.point.checkpoint.R;
import red.point.checkpoint.api.model.CompanyResponse;
import red.point.checkpoint.api.model.Employee;
import red.point.checkpoint.api.model.EmployeeResponse;
import red.point.checkpoint.api.service.CompanyService;
import red.point.checkpoint.api.service.EmployeeService;
import red.point.checkpoint.di.ContextModule;
import red.point.checkpoint.helper.FragmentHelper;
import red.point.checkpoint.helper.PrefManager;
import red.point.checkpoint.ui.registration.SetupActivity;
import red.point.checkpoint.util.ProgressDialogUtil;
import red.point.checkpoint.util.ToastUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class CompanyFragment extends Fragment {

    @Inject PrefManager prefManager;
    @Inject CompanyService companyService;
    @Inject EmployeeService employeeService;

    @BindView(R.id.shimmer_view_container) ShimmerFrameLayout shimmerFrameLayout;
    @BindView(R.id.name) TextView mName;
    @BindView(R.id.delete) LinearLayout btn_delete;
    @BindView(R.id.content) LinearLayout mContent;

    private Unbinder unbinder;
    private AlertDialog.Builder builder;
    private Call<EmployeeResponse> employeeResponseCall;
    private Call<CompanyResponse> companyResponseCall;

    public CompanyFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_company, container, false);

        unbinder = ButterKnife.bind(this, rootView);

        CompanyComponent companyComponent = DaggerCompanyComponent.builder()
                .companyModule(new CompanyModule())
                .contextModule(new ContextModule(getContext()))
                .build();

        companyComponent.inject(this);

        if (getActivity() != null) getActivity().setTitle(prefManager.getCompanyName());

        shimmerFrameLayout.startShimmerAnimation();

        employeeResponseCall = employeeService.getEmployee(prefManager.getCompanyId(), prefManager.getUserId());
        employeeResponseCall.enqueue(new Callback<EmployeeResponse>() {
            @Override
            public void onResponse(Call<EmployeeResponse> call, Response<EmployeeResponse> response) {
                shimmerFrameLayout.stopShimmerAnimation();
                shimmerFrameLayout.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Employee employee = response.body().getEmployee();
                    if (employee.getOwner()) {
                        btn_delete.setVisibility(View.VISIBLE);
                    } else {
                        btn_delete.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<EmployeeResponse> call, Throwable t) {
                if (!call.isCanceled()) {
                    shimmerFrameLayout.stopShimmerAnimation();
                    shimmerFrameLayout.setVisibility(View.GONE);
                }
            }
        });

        mName.setText(prefManager.getCompanyName());

        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (unbinder != null) unbinder.unbind();
        if (employeeResponseCall != null && employeeResponseCall.isExecuted()) employeeResponseCall.cancel();
        if (companyResponseCall != null && companyResponseCall.isExecuted()) companyResponseCall.cancel();

        ProgressDialogUtil.dismiss();
    }

    @OnClick(R.id.name)
    public void editNameDialog() {
        Bundle bundle = new Bundle();
        bundle.putString("name", mName.getText().toString());

        FragmentHelper.show(getActivity(), new EditCompanyNameFragment(), bundle);
    }

    @OnClick(R.id.delete)
    public void deleteCompanyDialog() {
        builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
        builder.setTitle("Delete Company")
                .setMessage(R.string.delete_company)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> deleteCompanyConfirmed())
                .setNegativeButton(android.R.string.no, (dialog, which) -> {})
                .setIcon(R.drawable.ic_warning_24dp)
                .show();
    }

    private void deleteCompanyConfirmed() {
        ProgressDialogUtil.showLoading(getContext());
        companyResponseCall = companyService.deleteCompany(prefManager.getCompanyId());
        companyResponseCall.enqueue(new Callback<CompanyResponse>() {
            @Override
            public void onResponse(Call<CompanyResponse> call, Response<CompanyResponse> response) {
                ProgressDialogUtil.dismiss();
                if (response.isSuccessful()) {
                    ToastUtil.show("Company deleted");

                    Intent intent = new Intent(getActivity(), SetupActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }

            @Override
            public void onFailure(Call<CompanyResponse> call, Throwable t) {
                if (! call.isCanceled()) {
                    ProgressDialogUtil.dismiss();
                    ToastUtil.show("Connection Failure");
                }
            }
        });
    }
}
