package red.point.checkpoint.ui.registration.referral;

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

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import red.point.checkpoint.R;
import red.point.checkpoint.api.model.ReferralResponse;
import red.point.checkpoint.api.model.User;
import red.point.checkpoint.api.service.ReferralService;
import red.point.checkpoint.di.ContextModule;
import red.point.checkpoint.helper.FragmentHelper;
import red.point.checkpoint.helper.PrefManager;
import red.point.checkpoint.ui.dialog.LottieDialogFragment;
import red.point.checkpoint.ui.home.HomeFragment;
import red.point.checkpoint.util.ProgressDialogUtil;
import red.point.checkpoint.util.ToastUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReferralDialogFragment extends DialogFragment {

    private static final String TAG = ReferralDialogFragment.class.getSimpleName();

    @Inject
    PrefManager prefManager;

    @Inject
    ReferralService referralService;

    @BindView(R.id.code)
    EditText mCode;

    @BindView(R.id.hint)
    TextView mHint;

    private Unbinder unbinder;

    private User user;
    private View rootView;
    private Call<ReferralResponse> referralResponseCall;
    private long referralSourceId;

    public ReferralDialogFragment() {}

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {

        rootView = getActivity().getLayoutInflater().inflate(R.layout.fragment_dialog_referral, null);

        unbinder = ButterKnife.bind(this, rootView);

        ReferralComponent referralComponent = DaggerReferralComponent.builder()
                .contextModule(new ContextModule(getContext()))
                .referralModule(new ReferralModule())
                .build();

        referralComponent.injectDialog(this);

        referralSourceId = getArguments().getLong("referral_source_id");

        return editDialog();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (unbinder != null) unbinder.unbind();

        if (referralResponseCall != null && referralResponseCall.isExecuted()) referralResponseCall.cancel();
    }

    private AlertDialog editDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setPositiveButton("Save", null);
        builder.setNegativeButton("Skip", null);
        builder.setNeutralButton("Back", (dialog, which) -> dismiss());

        builder.setView(rootView);
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialog1 -> {
            Button positiveButton = ((AlertDialog) dialog1).getButton(DialogInterface.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                if (TextUtils.isEmpty(mCode.getText())) {
                    ToastUtil.show("Referral code cannot be empty");
                    return;
                }

                String code = mCode.getText().toString().trim();

                ProgressDialogUtil.showLoading(getContext());

                referralResponseCall = referralService.store(
                        prefManager.getUserId(),
                        code,
                        referralSourceId
                );

                referralResponseCall.enqueue(new Callback<ReferralResponse>() {
                    @Override
                    public void onResponse(Call<ReferralResponse> call, Response<ReferralResponse> response) {
                        if (response.isSuccessful()) {
                            FragmentHelper.replace(getActivity(), new HomeFragment(), null, false);
                            FragmentHelper.show(getActivity(), new LottieDialogFragment(), null);
                        }

                        ProgressDialogUtil.dismiss();
                        dismiss();
                    }

                    @Override
                    public void onFailure(Call<ReferralResponse> call, Throwable t) {
                        if (!call.isCanceled()) {
                            ToastUtil.show("Connection Failure");
                            ProgressDialogUtil.dismiss();
                            dismiss();
                        }
                    }
                });

            });

            Button negativeButton = ((AlertDialog) dialog1).getButton(DialogInterface.BUTTON_NEGATIVE);
            negativeButton.setOnClickListener(v -> {
                ProgressDialogUtil.showLoading(getContext());

                referralResponseCall = referralService.store(
                        prefManager.getUserId(),
                        null,
                        referralSourceId
                );

                referralResponseCall.enqueue(new Callback<ReferralResponse>() {
                    @Override
                    public void onResponse(Call<ReferralResponse> call, Response<ReferralResponse> response) {
                        if (response.isSuccessful()) {
                            FragmentHelper.replace(getActivity(), new HomeFragment(), null, false);
                            FragmentHelper.show(getActivity(), new LottieDialogFragment(), null);
                        }

                        ProgressDialogUtil.dismiss();
                        dismiss();
                    }

                    @Override
                    public void onFailure(Call<ReferralResponse> call, Throwable t) {
                        if (!call.isCanceled()) {
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
}
