package red.point.checkpoint.ui.registration.setup;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import red.point.checkpoint.MyApplication;
import red.point.checkpoint.R;
import red.point.checkpoint.api.model.Invite;
import red.point.checkpoint.api.model.InviteList;
import red.point.checkpoint.api.service.EmployeeService;
import red.point.checkpoint.di.ContextModule;
import red.point.checkpoint.helper.PrefManager;
import red.point.checkpoint.ui.registration.IntroActivity;
import red.point.checkpoint.util.ProgressDialogUtil;
import red.point.checkpoint.util.ToastUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SetupEmployeeFragment extends Fragment {

    private static final String TAG = SetupEmployeeFragment.class.getSimpleName();

    private Unbinder unbinder;

    @Inject PrefManager prefManager;
    @Inject EmployeeService employeeService;
    @Inject EmployeeService joinCompanyService;

    @BindView(R.id.btn_join) Button btnJoin;
    @BindView(R.id.no_data) TextView mNoData;
    @BindView(R.id.invited_by) TextView mInvitedBy;

    private List<Invite> invites;
    private Invite invite;
    private Call<Invite> joinCompanyCall;
    private Call<InviteList> invitesCall;

    public SetupEmployeeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setup_employee, container, false);

        SetupComponent setupComponent = DaggerSetupComponent.builder()
                .contextModule(new ContextModule(getContext()))
                .setupModule(new SetupModule())
                .build();

        setupComponent.injectSetupEmployeeFragment(this);

        unbinder = ButterKnife.bind(this, view);

        ProgressDialogUtil.showLoading(getContext());
        invitesCall = employeeService.invited(prefManager.getUserEmail());
        invitesCall.enqueue(new Callback<InviteList>() {
            @Override
            public void onResponse(Call<InviteList> call, Response<InviteList> response) {
                if (response.isSuccessful()) {

                    invites = response.body().getInvites();

                    for (int i=0; i<invites.size(); i++) {
                        invite = invites.get(i);
                    }

                    if (invite != null) {
                        mNoData.setVisibility(View.INVISIBLE);
                        mInvitedBy.setVisibility(View.VISIBLE);
                        btnJoin.setVisibility(View.VISIBLE);
                        mInvitedBy.setText(String.format("You got invitation from %s\nPlease click join button accept this invitation", invite.getCompany().getName()));
                    }
                } else {
                    mInvitedBy.setVisibility(View.INVISIBLE);
                    btnJoin.setVisibility(View.INVISIBLE);
                    mNoData.setVisibility(View.VISIBLE);
                }

                ProgressDialogUtil.dismiss();
            }

            @Override
            public void onFailure(Call<InviteList> call, Throwable t) {
                if (! call.isCanceled()) {
                    ToastUtil.show(t.getMessage());
                    mInvitedBy.setVisibility(View.INVISIBLE);
                    btnJoin.setVisibility(View.INVISIBLE);
                    mNoData.setVisibility(View.VISIBLE);
                    ProgressDialogUtil.dismiss();
                }
            }
        });

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unbinder.unbind();

        if (joinCompanyCall != null && joinCompanyCall.isExecuted()) joinCompanyCall.cancel();
        if (invitesCall != null && invitesCall.isExecuted()) invitesCall.cancel();

        ProgressDialogUtil.dismiss();
    }

    @OnClick(R.id.btn_join)
    public void join() {
        ProgressDialogUtil.showLoading(getContext());

        btnJoin.setVisibility(View.INVISIBLE);

        joinCompanyCall = joinCompanyService.joinCompany(
                invite.getCompanyId(),
                invite.getId());

        joinCompanyCall.enqueue(new Callback<Invite>() {
            @Override
            public void onResponse(Call<Invite> call, Response<Invite> response) {
                ProgressDialogUtil.dismiss();
                if (response.isSuccessful()) {
                    ToastUtil.show("join success");

                    prefManager.setFirstTimeSeeWallet(true);
                    prefManager.setFirstTimeLaunchReport(true);
                    prefManager.setCompanyId(invite.getCompany().getId());
                    prefManager.setCompanyName(invite.getCompany().getName());

                    Intent intent = new Intent(MyApplication.getInstance().getActivity(), IntroActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    MyApplication.getInstance().getActivity().finish();
                }
            }

            @Override
            public void onFailure(Call<Invite> call, Throwable t) {
                if (! call.isCanceled()) {
                    ToastUtil.show(t.getLocalizedMessage());
                    Intent intent = new Intent(MyApplication.getInstance().getActivity(), SetupFragment.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    MyApplication.getInstance().getActivity().finish();
                    ProgressDialogUtil.dismiss();
                }
            }
        });
    }
}
