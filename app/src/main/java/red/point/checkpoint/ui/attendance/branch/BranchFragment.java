package red.point.checkpoint.ui.attendance.branch;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.shimmer.ShimmerFrameLayout;

import org.parceler.Parcels;

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
import red.point.checkpoint.api.service.BranchService;
import red.point.checkpoint.databinding.FragmentBranchBinding;
import red.point.checkpoint.di.ContextModule;
import red.point.checkpoint.helper.FragmentHelper;
import red.point.checkpoint.helper.PrefManager;
import red.point.checkpoint.helper.RecyclerItemClickListener;
import red.point.checkpoint.repository.BranchRepository;
import red.point.checkpoint.ui.MainFragment;
import red.point.checkpoint.util.ProgressDialogUtil;
import red.point.checkpoint.util.RecyclerViewUtil;
import red.point.checkpoint.viewmodel.BranchListViewModel;


public class BranchFragment extends MainFragment {

    private static final String TAG = BranchFragment.class.getSimpleName();

    @Inject
    BranchAdapter branchAdapter;

    @Inject
    PrefManager prefManager;

    @Inject
    BranchService branchService;

    private List<Branch> listBranch = new ArrayList<>();

    private Unbinder unbinder;

    @BindView(R.id.fabAdd) FloatingActionButton addButton;
    @BindView(R.id.emptyLayout) RelativeLayout emptyLayout;
    @BindView(R.id.emptyIcon) ImageView emptyIcon;
    @BindView(R.id.emptyText) TextView emptyText;
    @BindView(R.id.branch_list) RecyclerView recyclerView;
    @BindView(R.id.shimmer_view_container) ShimmerFrameLayout shimmerFrameLayout;

    private FragmentBranchBinding binding;

    public BranchFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Log.d(TAG, getClass().getSimpleName() + " Attach");

        if (getActivity() != null) {
            getActivity().setTitle(R.string.nav_branch);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, getClass().getSimpleName() + " Create");
    }

    private void observeBranchListViewModel(BranchListViewModel viewModel) {
        viewModel.getBranches().observe(this, branch -> {

            if (branch != null && branch.size() > 0) {
                listBranch.clear();
                listBranch.addAll(branch);
                binding.setBranches(branch.get(0));
            }

            recyclerView.setAdapter(branchAdapter);

            branchAdapter.notifyDataSetChanged();

            if (getActivity() != null) {
                updateView();

                shimmerFrameLayout.setVisibility(View.GONE);
                shimmerFrameLayout.stopShimmerAnimation();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // View view = inflater.inflate(R.layout.fragment_branch, container, false);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_branch, container, false);
        View view = binding.getRoot();

        BranchComponent branchComponent = DaggerBranchComponent.builder()
                .branchModule(new BranchModule(savedInstanceState, listBranch))
                .contextModule(new ContextModule(getContext()))
                .build();

        branchComponent.inject(this);

        unbinder = ButterKnife.bind(this, view);

        Log.d(TAG, getClass().getSimpleName() + " CreateView");

        // Add a shimmering effect to any view in your app.
        // It's useful as an unobtrusive loading indicator.
        shimmerFrameLayout.startShimmerAnimation();

        // Add empty layout when no data fetched
        emptyLayout.setVisibility(View.GONE);
        emptyIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_store));
        emptyText.setText(R.string.empty_branch);

        // Initiate recycler view
        RecyclerViewUtil.setDefault(getContext(), recyclerView);

        // Set recycler view adapter
        recyclerView.addOnItemTouchListener(recyclerItemClickListener);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        BranchListViewModel.Factory factory = new BranchListViewModel.Factory(
                getActivity().getApplication(),
                new BranchRepository(branchService),
                prefManager.getCompanyId());

        BranchListViewModel viewModel = ViewModelProviders.of(this, factory).get(BranchListViewModel.class);

        observeBranchListViewModel(viewModel);

        Log.d(TAG, getClass().getSimpleName() + " Start");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, getClass().getSimpleName() + " Stop");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, getClass().getSimpleName() + " Pause");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (unbinder != null) unbinder.unbind();

        ProgressDialogUtil.dismiss();

        Log.d(TAG, getClass().getSimpleName() + " DestroyView");
    }

    private void updateView() {
        if (listBranch.size() == 0) {
            emptyLayout.setVisibility(View.VISIBLE);
        } else {
            emptyLayout.setVisibility(View.INVISIBLE);
        }
    }

    @OnClick(R.id.fabAdd)
    void addBranch() {
        addButton.setEnabled(false);

        FragmentHelper.show(getActivity(), new CreateBranchFragment(), null);
    }

    private RecyclerItemClickListener recyclerItemClickListener = new RecyclerItemClickListener(getContext(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
        @Override public void onItemClick(View view, int position) {
            Bundle bundle = new Bundle();
            bundle.putParcelable("branch", Parcels.wrap(listBranch.get(position)));

            FragmentHelper.replace(Objects.requireNonNull(getActivity()), new BranchSingleFragment(), bundle, true);
        }

        @Override public void onLongItemClick(View view, int position) {

        }
    });
}
