package red.point.checkpoint.ui.company;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import red.point.checkpoint.MyApplication;
import red.point.checkpoint.R;
import red.point.checkpoint.api.model.CompanyResponse;
import red.point.checkpoint.api.service.CompanyService;
import red.point.checkpoint.di.ContextModule;
import red.point.checkpoint.helper.FragmentHelper;
import red.point.checkpoint.helper.PrefManager;
import red.point.checkpoint.util.ProgressDialogUtil;
import red.point.checkpoint.util.ToastUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditCompanyNameFragment extends DialogFragment {

    private static final String TAG = EditCompanyNameFragment.class.getSimpleName();

    @Inject
    PrefManager prefManager;
    @Inject
    CompanyService companyService;

    @BindView(R.id.name) EditText mName;

    private Unbinder unbinder;
    private Call<CompanyResponse> companyResponseCall;
    private String name;
    private View rootView;

    public EditCompanyNameFragment() {
        // Required empty public constructor
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {

        rootView = getActivity().getLayoutInflater().inflate(R.layout.fragment_edit_company_name, null);

        unbinder = ButterKnife.bind(this, rootView);

        CompanyComponent companyComponent = DaggerCompanyComponent.builder()
                .contextModule(new ContextModule(getContext()))
                .companyModule(new CompanyModule())
                .build();

        companyComponent.inject(this);

        prefManager = new PrefManager(MyApplication.getAppContext());

        mName.setText(getArguments().getString("name"));

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
        builder.setTitle("Edit Company Name");
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

                name = mName.getText().toString().trim();

                ProgressDialogUtil.showLoading(getContext());
                companyResponseCall = companyService.putCompany(prefManager.getCompanyId(), name);

                companyResponseCall.enqueue(new Callback<CompanyResponse>() {
                    @Override
                    public void onResponse(Call<CompanyResponse> call, Response<CompanyResponse> response) {
                        ProgressDialogUtil.dismiss();
                        if (response.isSuccessful()) {
                            ToastUtil.show("Company Name Updated");
                            dismiss();

                            prefManager.setCompanyName(name);

                            FragmentHelper.replace(getActivity(), new CompanyFragment(), null, false);
                        }
                    }

                    @Override
                    public void onFailure(Call<CompanyResponse> call, Throwable t) {
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
