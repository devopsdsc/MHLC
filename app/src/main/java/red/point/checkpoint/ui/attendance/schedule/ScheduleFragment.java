package red.point.checkpoint.ui.attendance.schedule;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
import butterknife.OnClick;
import butterknife.Unbinder;
import red.point.checkpoint.R;
import red.point.checkpoint.adapter.ScheduleAdapter;
import red.point.checkpoint.api.model.Employee;
import red.point.checkpoint.api.model.EmployeeList;
import red.point.checkpoint.api.model.Schedule;
import red.point.checkpoint.api.model.ScheduleList;
import red.point.checkpoint.api.model.ScheduleResponse;
import red.point.checkpoint.api.service.EmployeeService;
import red.point.checkpoint.api.service.ScheduleService;
import red.point.checkpoint.di.ContextModule;
import red.point.checkpoint.dialog.MonthYearPickerDialogFragment;
import red.point.checkpoint.helper.FragmentHelper;
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

public class ScheduleFragment extends MainFragment {

    private static final String TAG = ScheduleFragment.class.getSimpleName();

    @Inject PrefManager prefManager;
    @Inject ScheduleService scheduleService;
    @Inject ScheduleService deleteScheduleService;
    @Inject EmployeeService employeeService;

    @BindView(R.id.list_item) RecyclerView recyclerView;
    @BindView(R.id.shimmer_view_container) ShimmerFrameLayout shimmerFrameLayout;
    @BindView(R.id.date) TextView mDate;
    @BindView(R.id.employee) TextView mEmployee;
    @BindView(R.id.emptyLayout) RelativeLayout mEmptyLayout;
    @BindView(R.id.emptyIcon) ImageView mEmptyIcon;
    @BindView(R.id.emptyText) TextView mEmptyText;

    private Unbinder unbinder;
    private Call<EmployeeList> employeeListCall;
    private Call<ScheduleList> schedulesCall;
    private Call<ScheduleResponse> deleteScheduleCall;
    private ScheduleAdapter adapter;
    private List<Schedule> result = new ArrayList<>();
    private long userId;
    private String startDate = DateUtil.getCurrentDbDate();
    private String endDate = DateUtil.getCurrentDbDate();

    public ScheduleFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (getActivity() != null) getActivity().setTitle(R.string.nav_schedule);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_schedule, container, false);

        unbinder = ButterKnife.bind(this, view);

        ScheduleComponent scheduleComponent = DaggerScheduleComponent.builder()
                .contextModule(new ContextModule(getContext()))
                .scheduleModule(new ScheduleModule())
                .build();

        scheduleComponent.inject(this);

        // Add default schedule user
        userId = prefManager.getUserId();

        // Add empty layout when no data fetched
        mEmptyIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_calendar));
        mEmptyText.setText(R.string.empty_schedule);

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;

        startDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, 1);
        endDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        mDate.setText(String.format(Locale.getDefault(), "%02d/%04d", month, year));
        mEmployee.setText(prefManager.getUserName());
        // Add a shimmering effect to any view in your app.
        // It's useful as an unobtrusive loading indicator.
        shimmerFrameLayout.startShimmerAnimation();

        // Initiate recycler view
        RecyclerViewUtil.setDefault(getContext(), recyclerView);

        // Set adapter to recycler view
        adapter = new ScheduleAdapter(result);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnItemTouchListener(ScheduleClickListener);

        loadData();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (unbinder != null) unbinder.unbind();

        if (schedulesCall != null && schedulesCall.isExecuted()) schedulesCall.cancel();
        if (employeeListCall != null && employeeListCall.isExecuted()) employeeListCall.cancel();
        if (deleteScheduleCall != null && deleteScheduleCall.isExecuted()) deleteScheduleCall.cancel();

        ProgressDialogUtil.dismiss();
    }

    @OnClick(R.id.btn_add_schedule)
    public void addSchedule() {
        FragmentHelper.replace(getActivity(), new CreateScheduleS1Fragment(), null, true);
    }

    private void loadData() {
        schedulesCall = scheduleService.getSchedulesByUser(prefManager.getCompanyId(), userId, startDate, endDate);
        schedulesCall.enqueue(new Callback<ScheduleList>() {
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

    @OnClick(R.id.cv_date)
    public void changeDate() {
        MonthYearPickerDialogFragment pd = new MonthYearPickerDialogFragment();

        pd.setListener((view, year, month, dayOfMonth) -> {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month - 1);

            shimmerFrameLayout.setVisibility(View.VISIBLE);
            shimmerFrameLayout.startShimmerAnimation();

            startDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, 1);
            endDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, cal.getActualMaximum(Calendar.DAY_OF_MONTH));

            Log.d(TAG, "DATE CHOOSE");

            loadData();

            mDate.setText(String.format(Locale.getDefault(), "%02d/%04d", month, year));
        });

        pd.show(getFragmentManager(), "MonthYearPickerDialog");
    }

    @OnClick(R.id.cv_employee)
    public void changeEmployee() {
        ProgressDialogUtil.showLoading(getContext());
        final AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());
        builderSingle.setIcon(R.drawable.ic_employees);
        builderSingle.setTitle("Select employee");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.select_dialog_singlechoice);
        employeeListCall = employeeService.getEmployees(prefManager.getCompanyId());
        employeeListCall.enqueue(new Callback<EmployeeList>() {
            @Override
            public void onResponse(Call<EmployeeList> call, Response<EmployeeList> response) {
                ProgressDialogUtil.dismiss();
                shimmerFrameLayout.stopShimmerAnimation();
                shimmerFrameLayout.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    List<Employee> employees = response.body().getEmployees();
                    for (int i = 0; i < employees.size(); i++) {
                        Employee employee = employees.get(i);
                        arrayAdapter.add(employee.getName());
                        arrayAdapter.notifyDataSetChanged();
                    }

                    builderSingle.setNegativeButton("cancel", (dialog, which) -> dialog.dismiss());
                    builderSingle.setAdapter(arrayAdapter, (dialog, which) -> {
                        mEmployee.setText(employees.get(which).getName());
                        shimmerFrameLayout.setVisibility(View.VISIBLE);
                        shimmerFrameLayout.startShimmerAnimation();

                        userId = employees.get(which).getId();

                        loadData();
                    });
                    builderSingle.show();
                }
            }

            @Override
            public void onFailure(Call<EmployeeList> call, Throwable t) {
                ProgressDialogUtil.dismiss();
                if (! call.isCanceled()) {
                    ToastUtil.show("Connection Failure");
                    shimmerFrameLayout.stopShimmerAnimation();
                    shimmerFrameLayout.setVisibility(View.GONE);
                }
            }
        });
    }

    RecyclerItemClickListener ScheduleClickListener = new RecyclerItemClickListener(getContext(), recyclerView ,new RecyclerItemClickListener.OnItemClickListener() {
        @Override public void onItemClick(View view, int position) {
            deleteScheduleDialog(result.get(position).getId(), position);
        }

        @Override public void onLongItemClick(View view, int position) {
            // do whatever
        }
    });

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
}
