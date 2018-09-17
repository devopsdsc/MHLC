package red.point.checkpoint.ui.attendance.shift;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import red.point.checkpoint.R;
import red.point.checkpoint.adapter.ShiftAdapter;
import red.point.checkpoint.api.model.Shift;
import red.point.checkpoint.api.model.ShiftList;
import red.point.checkpoint.api.service.ShiftService;
import red.point.checkpoint.di.ContextModule;
import red.point.checkpoint.helper.FragmentHelper;
import red.point.checkpoint.helper.PrefManager;
import red.point.checkpoint.helper.RecyclerItemClickListener;
import red.point.checkpoint.ui.MainFragment;
import red.point.checkpoint.util.ProgressDialogUtil;
import red.point.checkpoint.util.RecyclerViewUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class ShiftFragment extends MainFragment {

    private static final String TAG = ShiftFragment.class.getSimpleName();

    @Inject PrefManager prefManager;
    @Inject ShiftService shiftService;

    @BindView(R.id.shimmer_view_container) ShimmerFrameLayout shimmerFrameLayout;
    @BindView(R.id.list_item) RecyclerView recyclerView;

    private Unbinder unbinder;
    private Call<ShiftList> shiftsCall;
    private ShiftAdapter adapter;
    private List<Shift> result = new ArrayList<>();

    public ShiftFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (getActivity() != null) getActivity().setTitle(R.string.nav_shift);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_shift, container, false);

        unbinder = ButterKnife.bind(this, view);

        ShiftComponent shiftComponent = DaggerShiftComponent.builder()
                .contextModule(new ContextModule(getContext()))
                .shiftModule(new ShiftModule())
                .build();

        shiftComponent.inject(this);

        // Add a shimmering effect to any view in your app.
        // It's useful as an unobtrusive loading indicator.
        shimmerFrameLayout.startShimmerAnimation();

        // Set adapter
        RecyclerViewUtil.setDefault(getContext(), recyclerView);
        adapter = new ShiftAdapter(result);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnItemTouchListener(recyclerItemClickListener);

        loadData();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unbinder.unbind();
        if (shiftsCall != null && shiftsCall.isExecuted()) shiftsCall.cancel();

        ProgressDialogUtil.dismiss();
    }

    private void loadData() {
        shiftsCall = shiftService.getShifts(prefManager.getCompanyId());
        shiftsCall.enqueue(new Callback<ShiftList>() {
            @Override
            public void onResponse(Call<ShiftList> call, Response<ShiftList> response) {
                if (response.isSuccessful()) {
                    List<Shift> shift = response.body().getShifts();
                    result.clear();
                    result.addAll(shift);
                    adapter.notifyDataSetChanged();
                }

                shimmerFrameLayout.setVisibility(View.GONE);
                shimmerFrameLayout.stopShimmerAnimation();
            }

            @Override
            public void onFailure(Call<ShiftList> call, Throwable t) {
                if (! call.isCanceled()) {
                    Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                    shimmerFrameLayout.stopShimmerAnimation();
                    shimmerFrameLayout.setVisibility(View.GONE);
                }
            }
        });
    }

    private RecyclerItemClickListener recyclerItemClickListener =
                new RecyclerItemClickListener(getContext(), recyclerView ,new RecyclerItemClickListener.OnItemClickListener() {
        @Override public void onItemClick(View view, int position) {
            Bundle bundle = new Bundle();
            bundle.putParcelable("shift", Parcels.wrap(result.get(position)));

            FragmentHelper.show(getActivity(), new EditShiftFragment(), bundle);
        }

        @Override public void onLongItemClick(View view, int position) {

        }
    });
}
