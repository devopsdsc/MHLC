package red.point.checkpoint.ui.account;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.shimmer.ShimmerFrameLayout;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import red.point.checkpoint.R;
import red.point.checkpoint.api.model.User;
import red.point.checkpoint.api.model.UserResponse;
import red.point.checkpoint.api.service.UserService;
import red.point.checkpoint.di.ContextModule;
import red.point.checkpoint.helper.PrefManager;
import red.point.checkpoint.ui.MainActivity;
import red.point.checkpoint.ui.registration.PhoneAuthActivity;
import red.point.checkpoint.util.ProgressDialogUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class AccountFragment extends Fragment {

    private Unbinder unbinder;

    private Call<UserResponse> userResponseCall;

    @Inject
    PrefManager prefManager;

    @Inject
    UserService userService;

    @BindView(R.id.name)
    TextView mName;

    @BindView(R.id.email)
    TextView mEmail;

    @BindView(R.id.referral_code)
    TextView mReferralCode;

    @BindView(R.id.verify_phone_number)
    LinearLayout mVerifyPhoneNumber;

    @BindView(R.id.shimmer_view_container)
    ShimmerFrameLayout shimmerFrameLayout;

    public AccountFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        unbinder = ButterKnife.bind(this, view);

        AccountComponent accountComponent = DaggerAccountComponent.builder()
                .contextModule(new ContextModule(getContext()))
                .accountModule(new AccountModule()).build();

        accountComponent.inject(this);

        if (getActivity() != null) getActivity().setTitle(getString(R.string.nav_account));

        shimmerFrameLayout.startShimmerAnimation();

        userResponseCall = userService.getUser(prefManager.getUserId());
        userResponseCall.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful()) {
                    User user = response.body().getUser();

                    mName.setText(user.getName());
                    mEmail.setText(user.getEmail());
                    mReferralCode.setText(user.getReferralCode());

                    if (user.getPhone() == null) {
                        mVerifyPhoneNumber.setVisibility(View.VISIBLE);
                    }
                }

                shimmerFrameLayout.stopShimmerAnimation();
                shimmerFrameLayout.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                if (!call.isCanceled()) {
                    shimmerFrameLayout.stopShimmerAnimation();
                    shimmerFrameLayout.setVisibility(View.GONE);
                }
            }
        });

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unbinder.unbind();

        ProgressDialogUtil.dismiss();

        if (userResponseCall != null && userResponseCall.isExecuted()) userResponseCall.cancel();
    }

    @OnClick(R.id.sign_out)
    public void signOut() {
        ((MainActivity) getActivity()).logout();
    }

    @OnClick(R.id.verify_phone_number)
    public void verifyPhone() {
        Intent intent = new Intent(getActivity(), PhoneAuthActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }
}
