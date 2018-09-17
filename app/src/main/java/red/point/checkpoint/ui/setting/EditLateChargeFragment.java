package red.point.checkpoint.ui.setting;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.parceler.Parcels;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import red.point.checkpoint.R;
import red.point.checkpoint.api.model.Setting;
import red.point.checkpoint.api.model.SettingResponse;
import red.point.checkpoint.api.service.SettingService;
import red.point.checkpoint.di.ContextModule;
import red.point.checkpoint.helper.FragmentHelper;
import red.point.checkpoint.helper.PrefManager;
import red.point.checkpoint.util.ProgressDialogUtil;
import red.point.checkpoint.util.ToastUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class EditLateChargeFragment extends DialogFragment {

    @Inject PrefManager prefManager;
    @Inject SettingService settingService;

    @BindView(R.id.lateCharge) EditText mEditText;

    private Unbinder unbinder;
    private Call<SettingResponse> settingCall;

    public EditLateChargeFragment() {}

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_edit_late_charge, null);

        unbinder = ButterKnife.bind(this, view);

        EditSettingComponent editSettingComponent = DaggerEditSettingComponent.builder()
                .contextModule(new ContextModule(getContext()))
                .editSettingModule(new EditSettingModule())
                .build();

        editSettingComponent.injectEditLateCharge(this);

        builder.setPositiveButton("Save", null);
        builder.setNegativeButton("Cancel", (dialog, which) -> dismiss());
        builder.setView(view);

        Setting setting = Parcels.unwrap(getArguments().getParcelable("setting"));

        mEditText.setText(String.format("%s", setting.getLateCharge()));

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialog1 -> {
            Button positiveButton = ((AlertDialog) dialog1).getButton(DialogInterface.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                ProgressDialogUtil.showLoading(getContext());

                settingCall = settingService.putSetting(prefManager.getCompanyId(),
                        setting.getCheckInRadius(),
                        setting.getLateRange(),
                        Double.parseDouble(mEditText.getText().toString().trim()),
                        setting.getMaxCharge(),
                        setting.getReward());

                settingCall.enqueue(new Callback<SettingResponse>() {
                    @Override
                    public void onResponse(Call<SettingResponse> call, Response<SettingResponse> response) {
                        if (response.isSuccessful()) {
                            ToastUtil.show("Setting Updated");

                            FragmentHelper.replace(getActivity(), new SettingFragment(), null, false);
                        }

                        ProgressDialogUtil.dismiss();
                        dismiss();
                    }

                    @Override
                    public void onFailure(Call<SettingResponse> call, Throwable t) {
                        if (! call.isCanceled()) {
                            ToastUtil.show("Connection Failure");
                            ProgressDialogUtil.dismiss();
                            dismiss();
                        }
                    }
                });
            });
        });

        return dialog;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unbinder.unbind();
        if (settingCall != null && settingCall.isExecuted()) settingCall.cancel();

        ProgressDialogUtil.dismiss();
    }
}
