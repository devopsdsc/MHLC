package red.point.checkpoint.ui.attendance.branch;


import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;

import com.facebook.shimmer.ShimmerFrameLayout;

import org.parceler.Parcels;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import red.point.checkpoint.R;
import red.point.checkpoint.adapter.BranchScheduleAdapter;
import red.point.checkpoint.api.model.Branch;
import red.point.checkpoint.api.model.Schedule;
import red.point.checkpoint.api.model.ScheduleList;
import red.point.checkpoint.api.model.ScheduleResponse;
import red.point.checkpoint.api.service.BranchService;
import red.point.checkpoint.di.ContextModule;
import red.point.checkpoint.helper.PrefManager;
import red.point.checkpoint.helper.RecyclerItemClickListener;
import red.point.checkpoint.util.ProgressDialogUtil;
import red.point.checkpoint.util.RecyclerViewUtil;
import red.point.checkpoint.util.ToastUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BranchScheduleFragment extends Fragment {

    private static final String TAG = BranchScheduleFragment.class.getSimpleName();

    @Inject PrefManager prefManager;
    @Inject BranchService branchService;

    @BindView(R.id.shimmer_view_container) ShimmerFrameLayout shimmerFrameLayout;
    @BindView(R.id.list_item) RecyclerView recyclerView;
    @BindView(R.id.no_data) TextView emptyList;

    private Unbinder unbinder;
    private BranchScheduleAdapter adapter;
    private Call<ScheduleList> scheduleListCall;
    private Call<ScheduleResponse> deleteScheduleCall;
    private List<Schedule> result = new ArrayList<>();

    private Branch branch;

    public BranchScheduleFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (getActivity() != null) getActivity().setTitle(R.string.nav_branch);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            view = inflater.inflate(R.layout.fragment_branch_schedule, container, false);
        } else{
            view = inflater.inflate(R.layout.fragment_branch_schedule_api_below_23, container, false);
        }

        unbinder = ButterKnife.bind(this, view);

        BranchScheduleComponent branchScheduleComponent = DaggerBranchScheduleComponent.builder()
                .contextModule(new ContextModule(getContext()))
                .branchScheduleModule(new BranchScheduleModule())
                .build();

        branchScheduleComponent.inject(this);

        branch = Parcels.unwrap(getArguments().getParcelable("branch"));

        if (getActivity() != null) getActivity().setTitle(branch.getName());

        // set Adapter
        RecyclerViewUtil.setDefault(getContext(), recyclerView);
        adapter = new BranchScheduleAdapter(result);
        recyclerView.setAdapter(adapter);

        String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        updateList(currentDate);

        CalendarView mCalendarView = view.findViewById(R.id.calendar);

        mCalendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);

            updateList(selectedDate);
        });

        recyclerView.addOnItemTouchListener(ScheduleClickListener);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unbinder.unbind();
        if (scheduleListCall != null && scheduleListCall.isExecuted()) scheduleListCall.cancel();
        if (deleteScheduleCall != null && deleteScheduleCall.isExecuted()) deleteScheduleCall.cancel();

        ProgressDialogUtil.dismiss();
    }

    private void loadView() {
        if (result.size() == 0) {
            emptyList.setVisibility(View.VISIBLE);
        } else {
            emptyList.setVisibility(View.GONE);
        }
    }

    private void updateList(final String selectedDate) {
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        shimmerFrameLayout.startShimmerAnimation();

        if (scheduleListCall != null && scheduleListCall.isExecuted()) scheduleListCall.cancel();

        result.clear();
        adapter.notifyDataSetChanged();

        scheduleListCall = branchService.getSchedules(prefManager.getCompanyId(), branch.getId(), selectedDate);
        scheduleListCall.enqueue(new Callback<ScheduleList>() {
            @Override
            public void onResponse(Call<ScheduleList> call, Response<ScheduleList> response) {
                shimmerFrameLayout.stopShimmerAnimation();
                shimmerFrameLayout.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    result.addAll(response.body().getSchedules());
                    adapter.notifyDataSetChanged();
                    loadView();
                }
            }

            @Override
            public void onFailure(Call<ScheduleList> call, Throwable t) {
                if (!call.isCanceled()) {
                    shimmerFrameLayout.stopShimmerAnimation();
                    shimmerFrameLayout.setVisibility(View.GONE);
                }
            }
        });

        adapter.notifyDataSetChanged();
    }

    RecyclerItemClickListener ScheduleClickListener = new RecyclerItemClickListener(getContext(), recyclerView ,new RecyclerItemClickListener.OnItemClickListener() {
        @Override public void onItemClick(View view, int position) {
            Schedule schedule = result.get(position);

            AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
            builder.setTitle("Delete schedule")
                    .setMessage("Are you sure you want to delete this schedule ?")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        ProgressDialogUtil.showLoading(getContext());

                        deleteScheduleCall = branchService.deleteSchedule(prefManager.getCompanyId(), schedule.getBranchId(), schedule.getId());
                        deleteScheduleCall.enqueue(new Callback<ScheduleResponse>() {
                            @Override
                            public void onResponse(Call<ScheduleResponse> call, Response<ScheduleResponse> response) {
                                ProgressDialogUtil.dismiss();
                                if (response.isSuccessful()) {
                                    ToastUtil.show("Schedule deleted");

                                    result.remove(position);
                                    adapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onFailure(Call<ScheduleResponse> call, Throwable t) {
                                if (! call.isCanceled()) {
                                    ProgressDialogUtil.dismiss();
                                    ToastUtil.show("Connection Failure");
                                }
                            }
                        });
                    })
                    .setNegativeButton(android.R.string.no, (dialog, which) -> {})
                    .setIcon(R.drawable.ic_warning_24dp)
                    .show();
        }

        @Override public void onLongItemClick(View view, int position) {
            //
        }
    });

}
