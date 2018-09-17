package red.point.checkpoint.ui.registration.referral;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import red.point.checkpoint.R;
import red.point.checkpoint.adapter.ReferralSourceAdapter;
import red.point.checkpoint.api.model.ReferralSource;
import red.point.checkpoint.api.model.ReferralSourceList;
import red.point.checkpoint.api.service.ReferralSourceService;
import red.point.checkpoint.di.ContextModule;
import red.point.checkpoint.helper.FragmentHelper;
import red.point.checkpoint.helper.PrefManager;
import red.point.checkpoint.helper.RecyclerItemClickListener;
import red.point.checkpoint.util.RecyclerViewUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class ReferralFragment extends Fragment {

    private static final String TAG = ReferralFragment.class.getSimpleName();

    private Unbinder unbinder;

    @Inject
    PrefManager prefManager;

    @Inject
    ReferralSourceService referralSourceService;

    private Call<ReferralSourceList> referralSourceListCall;

    @BindView(R.id.list)
    RecyclerView recyclerView;

    @BindView(R.id.content_view)
    LinearLayout contentView;

    private List<ReferralSource> result = new ArrayList<>();
    private ReferralSourceAdapter adapter;

    @BindView(R.id.shimmer_view_container)
    ShimmerFrameLayout shimmerFrameLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_referral, container, false);

        unbinder = ButterKnife.bind(this, rootView);

        contentView.setVisibility(View.GONE);

        // Add a shimmering effect to any view in your app.
        // It's useful as an unobtrusive loading indicator.
        shimmerFrameLayout.startShimmerAnimation();

        ReferralComponent referralComponent = DaggerReferralComponent.builder()
                .contextModule(new ContextModule(getContext()))
                .referralModule(new ReferralModule())
                .build();

        referralComponent.inject(this);

        // Initiate recycler view
        RecyclerViewUtil.setDefault(getContext(), recyclerView);
        adapter = new ReferralSourceAdapter(result);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnItemTouchListener(onTouchListener);

        loadData();

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (unbinder != null) unbinder.unbind();

        if (referralSourceListCall != null && referralSourceListCall.isExecuted()) referralSourceListCall.cancel();
    }

    private RecyclerItemClickListener onTouchListener = new RecyclerItemClickListener(getContext(), recyclerView ,new RecyclerItemClickListener.OnItemClickListener() {
        @Override public void onItemClick(View view, int position) {
            Bundle bundle = new Bundle();
            bundle.putLong("referral_source_id", result.get(position).getId());

            FragmentHelper.show(getActivity(), new ReferralDialogFragment(), bundle);
        }

        @Override
        public void onLongItemClick(View view, int position) {

        }
    });

    private void loadData() {
        referralSourceListCall = referralSourceService.getAll();
        referralSourceListCall.enqueue(new Callback<ReferralSourceList>() {
            @Override
            public void onResponse(Call<ReferralSourceList> call, Response<ReferralSourceList> response) {
                if (response.isSuccessful()) {
                    List<ReferralSource> referralSources = response.body().getReferralSourceList();
                    result.clear();
                    result.addAll(referralSources);
                    adapter.notifyDataSetChanged();
                }

                shimmerFrameLayout.stopShimmerAnimation();
                shimmerFrameLayout.setVisibility(View.GONE);

                loadView();
            }

            @Override
            public void onFailure(Call<ReferralSourceList> call, Throwable t) {
                if (! call.isCanceled()) {
                    shimmerFrameLayout.stopShimmerAnimation();
                    shimmerFrameLayout.setVisibility(View.GONE);
                    loadView();
                }
            }
        });
    }

    private void loadView() {
        if (result.size() == 0)
            contentView.setVisibility(View.GONE);
        else
            contentView.setVisibility(View.VISIBLE);
    }
}
