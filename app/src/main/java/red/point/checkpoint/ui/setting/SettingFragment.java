package red.point.checkpoint.ui.setting;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;

import org.parceler.Parcels;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import red.point.checkpoint.MyApplication;
import red.point.checkpoint.R;
import red.point.checkpoint.api.model.Setting;
import red.point.checkpoint.api.model.SettingResponse;
import red.point.checkpoint.api.service.SettingService;
import red.point.checkpoint.di.ContextModule;
import red.point.checkpoint.helper.FragmentHelper;
import red.point.checkpoint.helper.PrefManager;
import red.point.checkpoint.ui.MainFragment;
import red.point.checkpoint.util.NumberUtil;
import red.point.checkpoint.util.ProgressDialogUtil;
import retrofit2.Call;
import retrofit2.Response;

public class SettingFragment extends MainFragment {

    @Inject PrefManager prefManager;
    @Inject SettingService settingService;

    @BindView(R.id.shimmer_view_container) ShimmerFrameLayout shimmerFrameLayout;
    @BindView(R.id.reward) TextView mReward;
    @BindView(R.id.late_charge_value) TextView mLateCharge;
    @BindView(R.id.max_charge_value) TextView mMaxCharge;
    @BindView(R.id.late_range_value) TextView mLateRange;
    @BindView(R.id.check_in_radius_value) TextView mCheckInRadius;

    private Unbinder unbinder;
    private Call<SettingResponse> settingResponseCall;
    private Setting setting;

    public SettingFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        prefManager = new PrefManager(MyApplication.getAppContext());

        if (getActivity() != null) getActivity().setTitle(getString(R.string.setting));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        unbinder = ButterKnife.bind(this, view);

        SettingComponent settingComponent = DaggerSettingComponent.builder()
                .contextModule(new ContextModule(getContext()))
                .settingModule(new SettingModule())
                .build();

        settingComponent.inject(this);

        // Add a shimmering effect to any view in your app.
        // It's useful as an unobtrusive loading indicator.
        shimmerFrameLayout.startShimmerAnimation();

        settingResponseCall = settingService.getSetting(prefManager.getCompanyId());
        settingResponseCall.enqueue(new retrofit2.Callback<SettingResponse>() {
            @Override
            public void onResponse(Call<SettingResponse> call, Response<SettingResponse> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        setting = response.body().getSetting();

                        mReward.setText(String.format("%s", NumberUtil.getFormattedNumber(setting.getReward())));
                        mLateCharge.setText(String.format("%s", NumberUtil.getFormattedNumber(setting.getLateCharge())));
                        mMaxCharge.setText(String.format("%s", NumberUtil.getFormattedNumber(setting.getMaxCharge())));
                        mLateRange.setText(String.format("%s", NumberUtil.getFormattedNumber(setting.getLateRange())));
                        mCheckInRadius.setText(String.format("%s", NumberUtil.getFormattedNumber(setting.getCheckInRadius())));
                    }
                }

                shimmerFrameLayout.stopShimmerAnimation();
                shimmerFrameLayout.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<SettingResponse> call, Throwable t) {
                if (! call.isCanceled()) {
                    Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
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

        if (settingResponseCall != null && settingResponseCall.isExecuted()) settingResponseCall.cancel();

        ProgressDialogUtil.dismiss();
    }

    @OnClick(R.id.btn_reward)
    public void clickBtnReward() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("setting", Parcels.wrap(setting));

        FragmentHelper.show(getActivity(), new EditRewardFragment(), bundle);
    }

    @OnClick(R.id.btn_late_charge)
    public void clickBtnLateCharge() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("setting", Parcels.wrap(setting));

        FragmentHelper.show(getActivity(), new EditLateChargeFragment(), bundle);
    }

    @OnClick(R.id.btn_max_charge)
    public void clickBtnMaxCharge() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("setting", Parcels.wrap(setting));

        FragmentHelper.show(getActivity(), new EditMaxChargeFragment(), bundle);
    }

    @OnClick(R.id.btn_late_range)
    public void clickBtnLateRange() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("setting", Parcels.wrap(setting));

        FragmentHelper.show(getActivity(), new EditLateRangeFragment(), bundle);
    }

    @OnClick(R.id.btn_check_in_radius)
    public void clickBtnCheckInRadius() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("setting", Parcels.wrap(setting));

        FragmentHelper.show(getActivity(), new EditCheckInRadiusFragment(), bundle);
    }
}
