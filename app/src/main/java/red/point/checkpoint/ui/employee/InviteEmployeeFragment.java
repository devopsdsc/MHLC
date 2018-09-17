package red.point.checkpoint.ui.employee;


import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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
import red.point.checkpoint.api.model.Invite;
import red.point.checkpoint.api.service.EmployeeService;
import red.point.checkpoint.di.ContextModule;
import red.point.checkpoint.helper.FragmentHelper;
import red.point.checkpoint.helper.PrefManager;
import red.point.checkpoint.ui.wallet.WalletFragment;
import red.point.checkpoint.util.ProgressDialogUtil;
import red.point.checkpoint.util.ToastUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class InviteEmployeeFragment extends DialogFragment {

    private static final String TAG = InviteEmployeeFragment.class.getSimpleName();

    private Unbinder unbinder;

    @Inject PrefManager prefManager;
    @Inject EmployeeService inviteEmployeeService;

    @BindView(R.id.name) EditText mName;
    @BindView(R.id.email) EditText mEmail;

    private View view;
    private String name, email;

    private Call<Invite> inviteEmployeeCall;

    public InviteEmployeeFragment() {}


    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {

        view = getActivity().getLayoutInflater().inflate(R.layout.fragment_invite_employee, null);

        InviteEmployeeComponent inviteEmployeeComponent = DaggerInviteEmployeeComponent.builder()
                .contextModule(new ContextModule(getContext()))
                .inviteEmployeeModule(new InviteEmployeeModule())
                .build();

        inviteEmployeeComponent.inject(this);

        unbinder = ButterKnife.bind(this, view);

        if (getArguments().getBoolean("isWalletEnough")) {
            return inviteDialog();
        } else {
            return insufficientWalletDialog();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unbinder.unbind();
        if (inviteEmployeeCall != null && inviteEmployeeCall.isExecuted()) inviteEmployeeCall.cancel();
        ProgressDialogUtil.dismiss();
    }

    private AlertDialog inviteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("INVITE EMPLOYEE");
        builder.setPositiveButton("Invite", null);
        builder.setNegativeButton("Cancel", (dialog, which) -> dismiss());
        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialog1 -> {
            Button positiveButton = ((AlertDialog) dialog1).getButton(DialogInterface.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {

                name = mName.getText().toString().trim();
                email = mEmail.getText().toString().trim();

                if (TextUtils.isEmpty(name)) {
                    ToastUtil.show("Name cannot be empty");
                    return;
                }

                if (TextUtils.isEmpty(email)) {
                    ToastUtil.show("Email cannot be empty");
                    return;
                }

                if (! email.toLowerCase().contains("gmail.com")) {
                    ToastUtil.show("Currently we only support gmail account");
                    return;
                }

                if (email.equals(prefManager.getUserEmail())) {
                    ToastUtil.show("Cannot invite your own email");
                    return;
                }

                try {
                    inviteEmployee();
                } catch (android.content.ActivityNotFoundException ex) {
                    ToastUtil.show("There are no email clients installed");
                }
            });
        });

        return dialog;
    }

    private AlertDialog insufficientWalletDialog() {
        return new AlertDialog.Builder(MyApplication.getInstance().getActivity())
                .setIcon(R.drawable.ic_warning_24dp)
                .setTitle("Wallet insufficient")
                .setMessage("Unable to invite new employee due to insufficient balance. \n" +
                        "\n" +
                        "Please top up your wallet.\n" +
                        "\n" +
                        "Our pricing is only \n" +
                        "IDR 10.000/employee/month")
                .setPositiveButton("Topup Wallet", (dialog, whichButton) -> FragmentHelper.replace(getActivity(), new WalletFragment(), null, true))
                .setNegativeButton("Maybe Later", (dialog, whichButton) -> {})
                .create();
    }

    private void sendEmail() {
        Intent i = new Intent(Intent.ACTION_SENDTO);
        i.setType("message/rfc822");
        i.setData(Uri.parse("mailto:" + email));
        i.putExtra(Intent.EXTRA_SUBJECT, "Checkpoint invitation from " + prefManager.getCompanyName());
        i.putExtra(Intent.EXTRA_TEXT   , "Hello " + name + ", you are invited by "
                + prefManager.getCompanyName()
                + " to use this application for your attendance.\n\n" +
                "Please install the following link https://play.google.com/store/apps/details?id=red.point.checkpoint");
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        MyApplication.getInstance().getActivity().startActivity(Intent.createChooser(i, "Send invitation"));
    }

    private void inviteEmployee() {
        ProgressDialogUtil.showLoading(getContext());
        inviteEmployeeCall = inviteEmployeeService.invite(prefManager.getCompanyId(), prefManager.getUserId(), email);
        inviteEmployeeCall.enqueue(new Callback<Invite>() {
            @Override
            public void onResponse(Call<Invite> call, Response<Invite> response) {
                ProgressDialogUtil.dismiss();

                if (response.isSuccessful()) {
                    ToastUtil.show(name + " invited");
                    dismiss();
                    sendEmail();
                }
            }

            @Override
            public void onFailure(Call<Invite> call, Throwable t) {
                ProgressDialogUtil.dismiss();
                dismiss();
                if (! call.isCanceled()) {
                    ToastUtil.show(t.getLocalizedMessage());
                }
            }
        });
    }
}
