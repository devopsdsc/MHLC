package red.point.checkpoint.ui.wallet;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import red.point.checkpoint.R;
import red.point.checkpoint.api.model.VoucherResponse;
import red.point.checkpoint.api.service.VoucherService;
import red.point.checkpoint.di.ContextModule;
import red.point.checkpoint.helper.FragmentHelper;
import red.point.checkpoint.helper.PrefManager;
import red.point.checkpoint.util.NumberUtil;
import red.point.checkpoint.util.ProgressDialogUtil;
import red.point.checkpoint.util.ToastUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class AddVoucherFragment extends DialogFragment {

    private static final String TAG = AddVoucherFragment.class.getSimpleName();

    @Inject PrefManager prefManager;
    @Inject VoucherService voucherService;

    @BindView(R.id.code) EditText mCode;

    private Unbinder unbinder;
    private Call<VoucherResponse> voucherResponseCall;

    public AddVoucherFragment() {}

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_add_voucher, null);

        unbinder = ButterKnife.bind(this, view);

        AddVoucherComponent addVoucherComponent = DaggerAddVoucherComponent.builder()
                .addVoucherModule(new AddVoucherModule())
                .contextModule(new ContextModule(getContext()))
                .build();

        addVoucherComponent.inject(this);

        builder.setPositiveButton("Apply", null);
        builder.setNegativeButton("Cancel", (dialog, which) -> dismiss());
        builder.setView(view);

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialog1 -> {
            Button positiveButton = ((AlertDialog) dialog1).getButton(DialogInterface.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String code = mCode.getText().toString().trim();

                if (TextUtils.isEmpty(code)) {
                    Toast.makeText(getContext(), "Enter voucher code", Toast.LENGTH_SHORT).show();
                    return;
                }

                ProgressDialogUtil.showLoading(getContext());

                voucherResponseCall = voucherService.useVoucher(
                        prefManager.getCompanyId(),
                        code);

                voucherResponseCall.enqueue(new Callback<VoucherResponse>() {
                    @Override
                    public void onResponse(Call<VoucherResponse> call, Response<VoucherResponse> response) {
                        if (response.isSuccessful()) {
                            if (response.body().getValue() > 0) {
                                successDialog(response.body().getValue());

                                FragmentHelper.replace(getActivity(), new WalletFragment(), null, false);
                            } else {
                                errorDialog(response.body().getMessage());
                            }
                        }

                        ProgressDialogUtil.dismiss();
                        dismiss();
                    }

                    @Override
                    public void onFailure(Call<VoucherResponse> call, Throwable t) {
                        if (! call.isCanceled()) {
                            ToastUtil.showConnectionFailure();
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

        if (voucherResponseCall != null && voucherResponseCall.isExecuted()) voucherResponseCall.cancel();

        ProgressDialogUtil.dismiss();
    }

    private void successDialog(double value) {
        new AlertDialog.Builder(getActivity())
                .setTitle("Congratulation")
                .setMessage("Your voucher " + NumberUtil.getFormattedNumber(value) + " is added to your wallet.")
                .setPositiveButton("Close", (dialog, whichButton) -> {
                })
                .show();
    }

    private void errorDialog(String message) {
        new AlertDialog.Builder(getActivity())
                .setMessage(message)
                .setPositiveButton("Close", (dialog, whichButton) -> {})
                .show();
    }
}
