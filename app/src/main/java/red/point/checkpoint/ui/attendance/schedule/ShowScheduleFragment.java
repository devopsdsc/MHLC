package red.point.checkpoint.ui.attendance.schedule;


import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import red.point.checkpoint.R;
import red.point.checkpoint.adapter.ScheduleAdapter;
import red.point.checkpoint.api.model.Schedule;
import red.point.checkpoint.api.model.ScheduleList;
import red.point.checkpoint.api.model.ScheduleResponse;
import red.point.checkpoint.api.service.ScheduleService;
import red.point.checkpoint.di.ContextModule;
import red.point.checkpoint.helper.PrefManager;
import red.point.checkpoint.helper.RecyclerItemClickListener;
import red.point.checkpoint.ui.MainFragment;
import red.point.checkpoint.util.DateUtil;
import red.point.checkpoint.util.ProgressDialogUtil;
import red.point.checkpoint.util.RecyclerViewUtil;
import red.point.checkpoint.util.ToastUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShowScheduleFragment extends MainFragment {

    private static final String TAG = ShowScheduleFragment.class.getName();

    @Inject PrefManager prefManager;
    @Inject ScheduleService scheduleService;
    @Inject ScheduleService deleteScheduleService;

    @BindView(R.id.list_item) RecyclerView recyclerView;
    @BindView(R.id.employee) TextView mEmployee;
    @BindView(R.id.branch) TextView mBranch;
    @BindView(R.id.emptyLayout) RelativeLayout mEmptyLayout;
    @BindView(R.id.emptyIcon) ImageView mEmptyIcon;
    @BindView(R.id.emptyText) TextView mEmptyText;
    @BindView(R.id.shimmer_view_container) ShimmerFrameLayout shimmerFrameLayout;

    private Unbinder unbinder;
    private Call<ScheduleList> scheduleListCall;
    private Call<ScheduleResponse> deleteScheduleCall;
    private ScheduleAdapter adapter;
    private List<Schedule> result = new ArrayList<>();

    private String startDate = DateUtil.getCurrentDbDate();
    private String endDate = DateUtil.getCurrentDbDate();

    public ShowScheduleFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (getActivity() != null) getActivity().setTitle(getString(R.string.schedule));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_show_schedule, container, false);

        unbinder = ButterKnife.bind(this, view);

        ShowScheduleComponent showScheduleComponent = DaggerShowScheduleComponent.builder()
                .contextModule(new ContextModule(getContext()))
                .build();

        showScheduleComponent.inject(this);

        // Add a shimmering effect to any view in your app.
        // It's useful as an unobtrusive loading indicator.
        shimmerFrameLayout.startShimmerAnimation();

        mEmployee.setText(getArguments().getString("employeeName"));
        mBranch.setText(getArguments().getString("branchName"));

        // Add empty layout when no data fetched
        mEmptyIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_store));
        mEmptyText.setText(R.string.empty_branch);

        RecyclerViewUtil.setDefault(getContext(), recyclerView);
        adapter = new ScheduleAdapter(result);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnItemTouchListener(ScheduleClickListener);

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;

        startDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, 1);
        endDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, cal.getActualMaximum(Calendar.DAY_OF_MONTH));

        loadData();

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

    private void deleteScheduleDialog(long scheduleId, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
        builder.setTitle("Delete Schedule")
                .setMessage("Are you sure you want to delete this schedule ?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> deleteSchedule(scheduleId, position))
                .setNegativeButton(android.R.string.no, (dialog, which) -> {})
                .setIcon(R.drawable.ic_warning_24dp)
                .show();
    }

    private void deleteSchedule(long scheduleId, int position) {
        ProgressDialogUtil.showLoading(getContext());
        deleteScheduleCall = deleteScheduleService.deleteSchedule(prefManager.getCompanyId(), scheduleId);
        deleteScheduleCall.enqueue(new Callback<ScheduleResponse>() {
            @Override
            public void onResponse(Call<ScheduleResponse> call, Response<ScheduleResponse> response) {
                if (response.isSuccessful()) {
                    ToastUtil.show("Schedule deleted");
                    result.remove(position);
                    adapter.notifyDataSetChanged();
                }

                ProgressDialogUtil.dismiss();
            }

            @Override
            public void onFailure(Call<ScheduleResponse> call, Throwable t) {
                if (! call.isCanceled()) {
                    ProgressDialogUtil.dismiss();
                }
            }
        });
    }

    private void loadData() {
        scheduleListCall = scheduleService.getSchedulesByUser(
                prefManager.getCompanyId(),
                getArguments().getLong("employeeId"),
                startDate,
                endDate);

        scheduleListCall.enqueue(new Callback<ScheduleList>() {
            @Override
            public void onResponse(Call<ScheduleList> call, Response<ScheduleList> response) {
                if (response.isSuccessful()) {
                    List<Schedule> schedules = Objects.requireNonNull(response.body()).getSchedules();
                    result.clear();
                    result.addAll(schedules);
                    adapter.notifyDataSetChanged();
                }

                shimmerFrameLayout.setVisibility(View.GONE);
                shimmerFrameLayout.stopShimmerAnimation();

                loadView();
            }

            @Override
            public void onFailure(Call<ScheduleList> call, Throwable t) {
                if (! call.isCanceled()) {
                    Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                    shimmerFrameLayout.stopShimmerAnimation();
                    shimmerFrameLayout.setVisibility(View.GONE);
                }
            }
        });
    }

    private void loadView() {
        if (result.size() == 0) {
            mEmptyLayout.setVisibility(View.VISIBLE);
        } else {
            mEmptyLayout.setVisibility(View.GONE);
        }
    }

    RecyclerItemClickListener ScheduleClickListener = new RecyclerItemClickListener(getContext(), recyclerView ,new RecyclerItemClickListener.OnItemClickListener() {
        @Override public void onItemClick(View view, int position) {
            deleteScheduleDialog(result.get(position).getId(), position);
        }

        @Override public void onLongItemClick(View view, int position) {
            // do whatever
        }
    });
}
