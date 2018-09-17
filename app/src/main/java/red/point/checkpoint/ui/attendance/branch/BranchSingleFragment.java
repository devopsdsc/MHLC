package red.point.checkpoint.ui.attendance.branch;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;

import org.parceler.Parcels;

import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import red.point.checkpoint.R;
import red.point.checkpoint.api.model.Branch;
import red.point.checkpoint.api.model.BranchResponse;
import red.point.checkpoint.api.service.BranchService;
import red.point.checkpoint.di.ContextModule;
import red.point.checkpoint.helper.FragmentHelper;
import red.point.checkpoint.helper.PrefManager;
import red.point.checkpoint.ui.MainFragment;
import red.point.checkpoint.util.ProgressDialogUtil;
import red.point.checkpoint.util.ToastUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BranchSingleFragment extends MainFragment {

    private static final String TAG = BranchSingleFragment.class.getSimpleName();

    @Inject PrefManager prefManager;
    @Inject BranchService branchService;

    @BindView(R.id.shimmer_view_container) ShimmerFrameLayout shimmerFrameLayout;
    @BindView(R.id.name) TextView mName;
    @BindView(R.id.address) TextView mAddress;
    @BindView(R.id.map_view) MapView mapView;
    @BindView(R.id.content_view) LinearLayout contentView;

    private Unbinder unbinder;

    private Bundle mSavedInstanceState;

    private Branch branch;

    private Call<BranchResponse> branchCall;

    public BranchSingleFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (getActivity() != null) getActivity().setTitle(R.string.nav_branch);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_branch_single, container, false);

        unbinder = ButterKnife.bind(this, view);

        BranchSingleComponent branchSingleComponent = DaggerBranchSingleComponent.builder()
                .contextModule(new ContextModule(getContext()))
                .branchSingleModule(new BranchSingleModule())
                .build();

        branchSingleComponent.inject(this);

        branch = Parcels.unwrap(getArguments().getParcelable("branch"));

        mSavedInstanceState = savedInstanceState;

        contentView.setVisibility(View.INVISIBLE);

        // Add a shimmering effect to any view in your app.
        // It's useful as an unobtrusive loading indicator.
        shimmerFrameLayout.startShimmerAnimation();

        loadData();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();

        if (branchCall != null && branchCall.isExecuted()) branchCall.cancel();

        ProgressDialogUtil.dismiss();
    }

    private void loadData() {
        shimmerFrameLayout.setVisibility(View.GONE);
        shimmerFrameLayout.stopShimmerAnimation();

        mName.setText(branch.getName().trim());
        mAddress.setText(branch.getAddress().trim());

        mapView.getMapAsync((GoogleMap googleMap) -> {
            final LatLng coordinates = new LatLng(branch.getLatitude(), branch.getLongitude());
            googleMap.addMarker(new MarkerOptions().position(coordinates).title(branch.getName()));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 16));
            googleMap.getUiSettings().setScrollGesturesEnabled(false);
            googleMap.getUiSettings().setZoomGesturesEnabled(false);
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                    Objects.requireNonNull(getContext()), R.raw.style_json));

            mapView.onResume();
        });

        mapView.onCreate(mSavedInstanceState);

        shimmerFrameLayout.stopShimmerAnimation();
        shimmerFrameLayout.setVisibility(View.GONE);

        contentView.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.edit)
    public void onClickEditBranch() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("branch", Parcels.wrap(branch));

        FragmentHelper.show(getActivity(), new EditBranchFragment(), bundle);
    }

    @OnClick(R.id.branch_schedule)
    public void onClickBranchSchedule() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("branch", Parcels.wrap(branch));

        FragmentHelper.replace(getActivity(), new BranchScheduleFragment(), bundle, true);
    }

    @OnClick(R.id.delete)
    public void onDeleteBranch() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
        builder.setTitle("Delete branch")
                .setMessage("Are you sure you want to delete this branch ?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> deleteBranchConfirmed())
                .setNegativeButton(android.R.string.no, (dialog, which) -> {})
                .setIcon(R.drawable.ic_warning_24dp)
                .show();
    }

    private void deleteBranchConfirmed() {
        ProgressDialogUtil.showLoading(getContext());

        branchCall = branchService.deleteBranch(prefManager.getCompanyId(), branch.getId());
        branchCall.enqueue(new Callback<BranchResponse>() {
            @Override
            public void onResponse(Call<BranchResponse> call, Response<BranchResponse> response) {
                ProgressDialogUtil.dismiss();
                if (response.isSuccessful()) {
                    ToastUtil.show("Branch deleted");

                    FragmentHelper.popBackStack(getActivity(), BranchFragment.class.getSimpleName());
                }
            }

            @Override
            public void onFailure(Call<BranchResponse> call, Throwable t) {
                if (! call.isCanceled()) {
                    ProgressDialogUtil.dismiss();
                    ToastUtil.show("Connection Failure");
                }
            }
        });
    }
}
