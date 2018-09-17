package red.point.checkpoint.ui.attendance.report;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
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
import red.point.checkpoint.adapter.AttendanceReportAdapter;
import red.point.checkpoint.api.model.Employee;
import red.point.checkpoint.api.model.EmployeeList;
import red.point.checkpoint.api.model.Schedule;
import red.point.checkpoint.api.model.ScheduleList;
import red.point.checkpoint.api.model.Setting;
import red.point.checkpoint.api.service.EmployeeService;
import red.point.checkpoint.api.service.ScheduleService;
import red.point.checkpoint.di.ContextModule;
import red.point.checkpoint.dialog.MonthYearPickerDialogFragment;
import red.point.checkpoint.helper.PrefManager;
import red.point.checkpoint.ui.MainFragment;
import red.point.checkpoint.util.DateUtil;
import red.point.checkpoint.util.NumberUtil;
import red.point.checkpoint.util.ProgressDialogUtil;
import red.point.checkpoint.util.RecyclerViewUtil;
import red.point.checkpoint.util.ToastUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AttendanceReportFragment extends MainFragment {

    private static final String TAG = AttendanceReportFragment.class.getSimpleName();

    private String reportContent;

    private Unbinder unbinder;

    @Inject PrefManager prefManager;
    @Inject ScheduleService scheduleService;
    @Inject EmployeeService employeeService;

    @BindView(R.id.shimmer_view_container) ShimmerFrameLayout shimmerFrameLayout;
    @BindView(R.id.list_item) RecyclerView rvReport;
    @BindView(R.id.date) TextView mDate;
    @BindView(R.id.employee) TextView mEmployee;
    @BindView(R.id.emptyLayout) RelativeLayout emptyLayout;
    @BindView(R.id.emptyIcon) ImageView emptyIcon;
    @BindView(R.id.emptyText) TextView emptyText;
    @BindView(R.id.tv_total_reward) TextView mTotalReward;
    @BindView(R.id.tv_total_charge) TextView mTotalCharge;
    @BindView(R.id.tv_total_salary) TextView mTotalSalary;

    private AttendanceReportAdapter adapter;
    private Call<ScheduleList> scheduleListCall;
    private Call<EmployeeList> employeeListCall;
    private List<Schedule> result = new ArrayList<>();

    private long employeeId;
    private String employeeName;
    private String startDate;
    private String endDate;
    private double totalSalary = 0;
    private double totalCharge = 0;
    private double totalReward = 0;

    public AttendanceReportFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (getActivity() != null) getActivity().setTitle(getString(R.string.attendance_report));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_attendance_report, container, false);

        setHasOptionsMenu(true);

        AttendanceReportComponent attendanceReportComponent = DaggerAttendanceReportComponent.builder()
                .contextModule(new ContextModule(getContext()))
                .attendanceReportModule(new AttendanceReportModule())
                .build();

        attendanceReportComponent.inject(this);

        unbinder = ButterKnife.bind(this, view);

        shimmerFrameLayout.startShimmerAnimation();

        employeeId = prefManager.getUserId();
        employeeName = prefManager.getUserName();

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = 1;
        int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        startDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, day);
        endDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, lastDay);
        mDate.setText(String.format(Locale.getDefault(), "%02d/%02d", month, year));
        mEmployee.setText(employeeName);

        // Set adapter
        RecyclerViewUtil.setDefault(getContext(), rvReport);
        adapter = new AttendanceReportAdapter(result);
        rvReport.setAdapter(adapter);

        updateListAdapter();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu,inflater);
        inflater.inflate(R.menu.export, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.export :
            {
                sendReport();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();

        if (scheduleListCall != null && scheduleListCall.isExecuted()) scheduleListCall.cancel();
        if (employeeListCall != null && employeeListCall.isExecuted()) employeeListCall.cancel();

        ProgressDialogUtil.dismiss();
    }

    @OnClick(R.id.cv_employee)
    public void chooseEmployee() {
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
                if (response.isSuccessful()) {
                    List<Employee> employees = response.body().getEmployees();
                    for (int i = 0; i < employees.size(); i++) {
                        arrayAdapter.add(employees.get(i).getName());
                        arrayAdapter.notifyDataSetChanged();
                    }

                    builderSingle.setNegativeButton("cancel", (dialog, which) -> dialog.dismiss());
                    builderSingle.setAdapter(arrayAdapter, (dialog, which) -> {
                        employeeId = employees.get(which).getId();
                        employeeName = employees.get(which).getName();
                        mEmployee.setText(employees.get(which).getName());
                        updateListAdapter();
                    });
                    builderSingle.show();
                }

                shimmerFrameLayout.stopShimmerAnimation();
                shimmerFrameLayout.setVisibility(View.GONE);
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

    @OnClick(R.id.cv_date)
    public void chooseDate() {
        MonthYearPickerDialogFragment pd = new MonthYearPickerDialogFragment();

        pd.setListener((view, year, month, dayOfMonth) -> {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month - 1);

            startDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, 1);
            endDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            mDate.setText(String.format(Locale.getDefault(), "%02d/%04d", month, year));
            updateListAdapter();
        });

        pd.show(getFragmentManager(), "MonthYearPickerDialog");
    }

    private void loadView() {
        if (result.size() == 0) {
            emptyText.setText("There is no report in this month");
            emptyLayout.setVisibility(View.VISIBLE);
        } else {
            emptyLayout.setVisibility(View.GONE);
        }
    }

    private void updateListAdapter() {
        result.clear();
        adapter.notifyDataSetChanged();
        reportContent = "";

        mTotalCharge.setText("-");
        mTotalReward.setText("-");
        mTotalSalary.setText("-");

        shimmerFrameLayout.setVisibility(View.VISIBLE);
        shimmerFrameLayout.startShimmerAnimation();

        scheduleListCall = scheduleService.getSchedulesByUser(
                prefManager.getCompanyId(),
                employeeId,
                startDate,
                endDate);

        scheduleListCall.enqueue(new Callback<ScheduleList>() {
            @Override
            public void onResponse(Call<ScheduleList> call, Response<ScheduleList> response) {
                if (response.isSuccessful()) {
                    List<Schedule> schedules = Objects.requireNonNull(response.body()).getSchedules();

                    if (schedules.size() > 0) {
                        Setting setting = response.body().getMeta().getSetting();

                        adapter.setMaxCharge(setting.getMaxCharge());

                        totalSalary = 0;
                        totalCharge = 0;
                        totalReward = 0;

                        for (int i = 0; i < schedules.size(); i++) {
                            Schedule schedule = schedules.get(i);

                            result.add(schedule);
                            adapter.notifyDataSetChanged();

                            double charge = schedule.getCharge() <= setting.getMaxCharge() ? schedule.getCharge() : setting.getMaxCharge();
                            totalCharge += charge;
                            totalReward += schedule.getReward();
                            totalSalary += schedule.getSalary();

                            reportContent = reportContent.concat(schedule.getDate() + ",");
                            reportContent = reportContent.concat(schedule.getShiftStart() + ",");
                            reportContent = reportContent.concat(schedule.getShiftEnd() + ",");

                            if (schedule.getCheckIn() != null) {
                                reportContent = reportContent.concat(DateUtil.timestampToTime(schedule.getCheckIn()) + ",");
                            } else {
                                reportContent = reportContent.concat("-,");
                            }

                            if (schedule.getCheckOut() != null) {
                                reportContent = reportContent.concat(DateUtil.timestampToTime(schedule.getCheckOut()) + ",");
                            } else {
                                reportContent = reportContent.concat("-,");
                            }

                            if (schedule.getCheckInLate() != null && schedule.getCheckInLate() > 0) {
                                reportContent = reportContent.concat(schedule.getCheckInLate() + ",");
                            } else {
                                reportContent = reportContent.concat("-,");
                            }

                            if (schedule.getCheckOutLate() != null && schedule.getCheckOutLate() > 0) {
                                reportContent = reportContent.concat(schedule.getCheckOutLate() + ",");
                            } else {
                                reportContent = reportContent.concat("-,");
                            }

                            reportContent = reportContent.concat(String.valueOf(schedule.getCharge()) + ",");
                            reportContent = reportContent.concat(String.valueOf(schedule.getReward()) + ",");
                            reportContent = reportContent.concat(String.valueOf(schedule.getSalary()) + ",");

                            reportContent = reportContent.concat("\r\n");

                            if (i + 1 == schedules.size()) {
                                if (schedule.getEmployee().getSalaryType().toLowerCase().equals("monthly")) {
                                    totalSalary = schedule.getEmployee().getSalary() + totalReward - totalCharge;
                                } else {
                                    totalSalary = totalSalary + totalReward - totalCharge;
                                }
                                reportContent = reportContent.concat(",,,,,,," + totalCharge + "," + totalReward + "," + totalSalary +"\r\n");
                            }
                        }

                        mTotalSalary.setText(String.format("%s", NumberUtil.getFormattedNumber(totalSalary)));
                        mTotalReward.setText(String.format("%s", NumberUtil.getFormattedNumber(totalReward)));
                        mTotalCharge.setText(String.format("%s", NumberUtil.getFormattedNumber(totalCharge)));
                    }
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

    private void sendReport() {
        String reportFileName = "ATTENDANCE REPORT " + startDate + " - " + endDate + " " + employeeName;

        try {
            String reportHeader = "DATE, SHIFT START, SHIFT END, CHECK IN, CHECK OUT, CHECK IN LATE, CHECK OUT LATE, CHARGE, REWARD, SALARY";
            File file = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "/" + reportFileName +".csv");

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(reportHeader + "\r\n");
            bw.write(reportContent + "\r\n");
            bw.close();

            Uri u1;
            u1 = Uri.fromFile(file);
            if(Build.VERSION.SDK_INT>=24){
                try{
                    Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                    m.invoke(null);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, "" + reportFileName);
            sendIntent.putExtra(Intent.EXTRA_STREAM, u1);
            sendIntent.setType("text/html");
            startActivity(Intent.createChooser(sendIntent, "SEND REPORT"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
