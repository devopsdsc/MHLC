package red.point.checkpoint.ui.attendance.schedule;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import red.point.checkpoint.R;
import red.point.checkpoint.adapter.BranchAdapter;
import red.point.checkpoint.api.model.Branch;
import red.point.checkpoint.api.model.BranchList;
import red.point.checkpoint.api.service.BranchService;
import red.point.checkpoint.di.ContextModule;
import red.point.checkpoint.helper.FragmentHelper;
import red.point.checkpoint.helper.PrefManager;
import red.point.checkpoint.helper.RecyclerItemClickListener;
import red.point.checkpoint.ui.MainFragment;
import red.point.checkpoint.util.ProgressDialogUtil;
import red.point.checkpoint.util.RecyclerViewUtil;
import red.point.checkpoint.util.ToastUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class CreateScheduleS2Fragment extends MainFragment {

    private static final String TAG = CreateScheduleS2Fragment.class.getSimpleName();

    @Inject PrefManager prefManager;
    @Inject BranchService branchService;

    @BindView(R.id.shimmer_view_container) ShimmerFrameLayout shimmerFrameLayout;
    @BindView(R.id.list_item) RecyclerView recyclerView;
    @BindView(R.id.emptyLayout) RelativeLayout mEmptyLayout;
    @BindView(R.id.emptyIcon) ImageView mEmptyIcon;
    @BindView(R.id.emptyText) TextView mEmptyText;
    @BindView(R.id.employee) TextView mEmployee;

    private Unbinder unbinder;
    private Call<BranchList> branchListCall;
    private BranchAdapter adapter;
    private List<Branch> result = new ArrayList<>();

    public CreateScheduleS2Fragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (getActivity() != null) getActivity().setTitle("Add Schedule");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_create_schedule_s2, container, false);

        unbinder = ButterKnife.bind(this, view);

        CreateScheduleS2Component createScheduleS2Component = DaggerCreateScheduleS2Component.builder()
                .createScheduleS2Module(new CreateScheduleS2Module())
                .contextModule(new ContextModule(getContext()))
                .build();

        createScheduleS2Component.inject(this);

        mEmployee.setText(getArguments().getString("employeeName"));

        // Add a shimmering effect to any view in your app.
        // It's useful as an unobtrusive loading indicator.
        shimmerFrameLayout.startShimmerAnimation();

        // Add empty layout when no data fetched
        mEmptyIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_store));
        mEmptyText.setText(R.string.empty_branch);

        // Setup recycler view
        RecyclerViewUtil.setDefault(getContext(), recyclerView);
        adapter = new BranchAdapter(getContext(), savedInstanceState, result);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnItemTouchListener(recyclerItemClickListener);

        loadData();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();

        if (branchListCall != null && branchListCall.isExecuted()) branchListCall.cancel();

        ProgressDialogUtil.dismiss();
    }

    private void loadData() {
        branchListCall = branchService.getBranchList(prefManager.getCompanyId());
        branchListCall.enqueue(new Callback<BranchList>() {
            @Override
            public void onResponse(Call<BranchList> call, Response<BranchList> response) {
                if (response.isSuccessful()) {
                    List<Branch> branch = Objects.requireNonNull(response.body()).getBranches();
                    result.clear();
                    result.addAll(branch);
                    adapter.notifyDataSetChanged();
                }

                shimmerFrameLayout.setVisibility(View.GONE);
                shimmerFrameLayout.stopShimmerAnimation();

                loadView();
            }

            @Override
            public void onFailure(Call<BranchList> call, Throwable t) {
                if (! call.isCanceled()) {
                    ToastUtil.showConnectionFailure();
                    shimmerFrameLayout.stopShimmerAnimation();
                    shimmerFrameLayout.setVisibility(View.GONE);
                }
            }
        });
    }

    @OnClick(R.id.cv_employee)
    public void clickBtnEmployee() {
        FragmentHelper.popBackStack(getActivity(), CreateScheduleS1Fragment.class.getSimpleName());
    }

    @OnClick(R.id.cv_branch)
    public void clickBtnBranch() {
        ToastUtil.show("Choose branch");
    }

    private RecyclerItemClickListener recyclerItemClickListener =
            new RecyclerItemClickListener(getContext(), recyclerView ,new RecyclerItemClickListener.OnItemClickListener() {
                @Override public void onItemClick(View view, int position) {
                    Bundle bundle = new Bundle();
                    bundle.putLong("employeeId", getArguments().getLong("employeeId"));
                    bundle.putString("employeeName", getArguments().getString("employeeName"));
                    bundle.putLong("branchId", result.get(position).getId());
                    bundle.putString("branchName", result.get(position).getName());

                    FragmentHelper.replace(getActivity(), new CreateScheduleFragment(), bundle, true);
                }

                @Override public void onLongItemClick(View view, int position) {
                    // do whatever
                }
            });

    private void loadView() {
        if (result.size() == 0) {
            mEmptyLayout.setVisibility(View.VISIBLE);
        } else {
            mEmptyLayout.setVisibility(View.GONE);
        }
    }
}
