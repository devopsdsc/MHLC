package red.point.checkpoint.ui.registration.setup;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.TimeZone;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import red.point.checkpoint.R;
import red.point.checkpoint.api.model.Company;
import red.point.checkpoint.api.model.CompanyResponse;
import red.point.checkpoint.api.service.CompanyService;
import red.point.checkpoint.di.ContextModule;
import red.point.checkpoint.helper.PrefManager;
import red.point.checkpoint.ui.registration.IntroActivity;
import red.point.checkpoint.util.ProgressDialogUtil;
import red.point.checkpoint.util.ToastUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SetupCompanyFragment extends Fragment {

    private static final String TAG = SetupCompanyFragment.class.getSimpleName();

    private Unbinder unbinder;

    @Inject PrefManager prefManager;
    @Inject CompanyService companyService;

    @BindView(R.id.name) EditText mName;

    private Call<CompanyResponse> storeCompanyCall;
    private String name;

    public SetupCompanyFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setup_company, container, false);

        unbinder = ButterKnife.bind(this, view);

        SetupComponent setupComponent = DaggerSetupComponent.builder()
                .contextModule(new ContextModule(getContext()))
                .setupModule(new SetupModule())
                .build();

        setupComponent.injectSetupCompanyFragment(this);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unbinder.unbind();

        if (storeCompanyCall != null && storeCompanyCall.isExecuted()) storeCompanyCall.cancel();

        ProgressDialogUtil.dismiss();
    }

    private void createCompany() {
        ProgressDialogUtil.showLoading(getContext());
        storeCompanyCall = companyService.storeCompany(name, prefManager.getUserId(), TimeZone.getDefault().getDisplayName());
        storeCompanyCall.enqueue(new Callback<CompanyResponse>() {
            @Override
            public void onResponse(Call<CompanyResponse> call, Response<CompanyResponse> response) {
                if (response.isSuccessful()) {
                    ToastUtil.show("Company created");

                    Company company = response.body().getCompany();

                    prefManager.setFirstTimeSeeWallet(true);
                    prefManager.setFirstTimeLaunchReport(true);
                    prefManager.setCompanyId(company.getId());
                    prefManager.setCompanyName(company.getName());
                    ProgressDialogUtil.dismiss();

                    Intent intent = new Intent(getActivity(), IntroActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    getActivity().finish();
                } else {
                    ToastUtil.show("Please use different name");
                    ProgressDialogUtil.dismiss();
                }
            }

            @Override
            public void onFailure(Call<CompanyResponse> call, Throwable t) {
                if (! call.isCanceled()) {
                    ToastUtil.show(t.getLocalizedMessage());
                    ProgressDialogUtil.dismiss();
                }
            }
        });
    }

    @OnClick(R.id.btn_create)
    public void setBtnCreate() {
        name = mName.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            ToastUtil.show("Enter company name");
            return;
        }

        if (name.length() < 3) {
            ToastUtil.show("Min name length is 3 digit");
            return;
        }

        if (name.length() > 20) {
            ToastUtil.show("Max name length is 20 digit");
            return;
        }

        createCompany();
    }
}
