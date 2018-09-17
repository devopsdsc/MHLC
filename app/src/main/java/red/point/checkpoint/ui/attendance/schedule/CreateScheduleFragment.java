package red.point.checkpoint.ui.attendance.schedule;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;

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
import red.point.checkpoint.api.model.ScheduleResponse;
import red.point.checkpoint.api.model.Shift;
import red.point.checkpoint.api.model.ShiftList;
import red.point.checkpoint.api.service.ScheduleService;
import red.point.checkpoint.api.service.ShiftService;
import red.point.checkpoint.di.ContextModule;
import red.point.checkpoint.helper.FragmentHelper;
import red.point.checkpoint.helper.PrefManager;
import red.point.checkpoint.ui.MainFragment;
import red.point.checkpoint.util.DateUtil;
import red.point.checkpoint.util.ProgressDialogUtil;
import red.point.checkpoint.util.ToastUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class CreateScheduleFragment extends MainFragment {

    private static final String TAG = CreateScheduleFragment.class.getSimpleName();

    @Inject PrefManager prefManager;
    @Inject ShiftService shiftService;
    @Inject ScheduleService scheduleService;

    @BindView(R.id.date) EditText mDate;
    @BindView(R.id.shiftStart) EditText mShiftStart;
    @BindView(R.id.shiftEnd) EditText mShiftEnd;
    @BindView(R.id.shift1) Button mShift1;
    @BindView(R.id.shift2) Button mShift2;
    @BindView(R.id.shift3) Button mShift3;
    @BindView(R.id.shift4) Button mShift4;
    @BindView(R.id.shift5) Button mShift5;
    @BindView(R.id.shift6) Button mShift6;
    @BindView(R.id.no_shift) Button mNoShift;
    @BindView(R.id.save) Button mSave;
    @BindView(R.id.content_view) RelativeLayout contentView;
    @BindView(R.id.employee) TextView mEmployee;
    @BindView(R.id.branch) TextView mBranch;
    @BindView(R.id.shimmer_view_container) ShimmerFrameLayout shimmerFrameLayout;

    private Unbinder unbinder;
    private String sDate;
    private String shiftStart;
    private String shiftEnd;
    private List<Shift> shifts;
    private Call<ScheduleResponse> storeScheduleCall;
    private Call<ShiftList> shiftsCall;

    public CreateScheduleFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (getActivity() != null) getActivity().setTitle("Add Schedule");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_create_schedule, container, false);

        unbinder = ButterKnife.bind(this, view);

        CreateScheduleComponent createScheduleComponent = DaggerCreateScheduleComponent.builder()
                .createScheduleModule(new CreateScheduleModule())
                .contextModule(new ContextModule(getContext()))
                .build();

        createScheduleComponent.inject(this);

        mEmployee.setText(getArguments().getString("employeeName"));
        mBranch.setText(getArguments().getString("branchName"));

        // Add a shimmering effect to any view in your app.
        // It's useful as an unobtrusive loading indicator.
        shimmerFrameLayout.startShimmerAnimation();

        contentView.setVisibility(View.GONE);

        String prevShiftStart = getArguments().getString("shiftStart");
        if (!TextUtils.isEmpty(prevShiftStart)) {
            mShiftStart.setText(prevShiftStart);
        }

        String prevShiftEnd = getArguments().getString("shiftEnd");
        if (!TextUtils.isEmpty(prevShiftEnd)) {
            mShiftEnd.setText(prevShiftEnd);
        }

        String date = getArguments().getString("date");
        if (date != null) {
            mDate.setText(date);
        }

        shiftsCall = shiftService.getShifts(prefManager.getCompanyId());
        shiftsCall.enqueue(new Callback<ShiftList>() {
            @Override
            public void onResponse(@NonNull Call<ShiftList> call, Response<ShiftList> response) {
                if (response.isSuccessful()) {
                    shifts = Objects.requireNonNull(response.body()).getShifts();

                    for (int i = 0; i < shifts.size(); i++) {
                        switch (i) {
                            case 0:
                                mShift1.setText(String.format("%s - %s", shifts.get(i).getShiftStart(), shifts.get(i).getShiftEnd()));
                                break;
                            case 1:
                                mShift2.setText(String.format("%s - %s", shifts.get(i).getShiftStart(), shifts.get(i).getShiftEnd()));
                                break;
                            case 2:
                                mShift3.setText(String.format("%s - %s", shifts.get(i).getShiftStart(), shifts.get(i).getShiftEnd()));
                                break;
                            case 3:
                                mShift4.setText(String.format("%s - %s", shifts.get(i).getShiftStart(), shifts.get(i).getShiftEnd()));
                                break;
                            case 4:
                                mShift5.setText(String.format("%s - %s", shifts.get(i).getShiftStart(), shifts.get(i).getShiftEnd()));
                                break;
                            case 5:
                                mShift6.setText(String.format("%s - %s", shifts.get(i).getShiftStart(), shifts.get(i).getShiftEnd()));
                                break;
                        }
                    }
                }

                shimmerFrameLayout.setVisibility(View.GONE);
                shimmerFrameLayout.stopShimmerAnimation();
                contentView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Call<ShiftList> call, Throwable t) {
                if (! call.isCanceled()) {
                    Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                    shimmerFrameLayout.stopShimmerAnimation();
                    shimmerFrameLayout.setVisibility(View.GONE);
                    contentView.setVisibility(View.VISIBLE);
                }
            }
        });

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (unbinder != null) unbinder.unbind();

        if (storeScheduleCall != null && storeScheduleCall.isExecuted()) storeScheduleCall.cancel();
        if (shiftsCall != null && shiftsCall.isExecuted()) shiftsCall.cancel();

        ProgressDialogUtil.dismiss();
    }

    @OnClick(R.id.showSchedule)
    public void showSchedule() {
        Bundle bundle = new Bundle();
        sDate = mDate.getText().toString().trim();
        shiftStart = mShiftStart.getText().toString().trim();
        shiftEnd = mShiftEnd.getText().toString().trim();
        bundle.putString("date", sDate);
        bundle.putString("shiftStart", shiftStart);
        bundle.putString("shiftEnd", shiftEnd);
        bundle.putLong("employeeId", getArguments().getLong("employeeId"));
        bundle.putString("employeeName", getArguments().getString("employeeName"));
        bundle.putLong("branchId", getArguments().getLong("branchId"));
        bundle.putString("branchName", getArguments().getString("branchName"));

        FragmentHelper.replace(getActivity(), new ShowScheduleFragment(), bundle, true);
    }

    @OnClick(R.id.shift1)
    public void setShift1() {
        mShiftStart.setText(shifts.get(0).getShiftStart());
        mShiftEnd.setText(shifts.get(0).getShiftEnd());
    }

    @OnClick(R.id.shift2)
    public void setShift2() {
        mShiftStart.setText(shifts.get(1).getShiftStart());
        mShiftEnd.setText(shifts.get(1).getShiftEnd());
    }

    @OnClick(R.id.shift3)
    public void setShift3() {
        mShiftStart.setText(shifts.get(2).getShiftStart());
        mShiftEnd.setText(shifts.get(2).getShiftEnd());
    }

    @OnClick(R.id.shift4)
    public void setShift4() {
        mShiftStart.setText(shifts.get(3).getShiftStart());
        mShiftEnd.setText(shifts.get(3).getShiftEnd());
    }

    @OnClick(R.id.shift5)
    public void setShift5() {
        mShiftStart.setText(shifts.get(4).getShiftStart());
        mShiftEnd.setText(shifts.get(4).getShiftEnd());
    }

    @OnClick(R.id.shift6)
    public void setShift6() {
        mShiftStart.setText(shifts.get(5).getShiftStart());
        mShiftEnd.setText(shifts.get(5).getShiftEnd());
    }

    @OnClick(R.id.date)
    public void chooseDate() {
        // calender class's instance and get current date , month and year from calender
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR); // current year
        int mMonth = c.get(Calendar.MONTH); // current month
        int mDay = c.get(Calendar.DATE); // current day
        // date picker dialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                (view1, year, monthOfYear, dayOfMonth) -> {
                    // set day of month , month and year value in the edit text
                    mDate.setText(String.format(Locale.getDefault(),
                            "%02d/%02d/%04d",
                            dayOfMonth, monthOfYear + 1, year));
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    @OnClick(R.id.shiftStart)
    public void shiftStart() {
        Calendar mCurrentTime = Calendar.getInstance();
        int hour = mCurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mCurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(getActivity(), (timePicker, selectedHour, selectedMinute) ->
                mShiftStart.setText(String.format(Locale.getDefault(),
                        "%02d:%02d", selectedHour, selectedMinute)), hour, minute, true);
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }

    @OnClick(R.id.shiftEnd)
    public void shiftEnd() {
        Calendar mCurrentTime = Calendar.getInstance();
        int hour = mCurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mCurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(getActivity(), (timePicker, selectedHour, selectedMinute) ->
                mShiftEnd.setText(String.format(Locale.getDefault(),
                        "%02d:%02d", selectedHour, selectedMinute)), hour, minute, true);
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }

    @OnClick(R.id.save)
    public void save() {
        sDate = mDate.getText().toString().trim();
        shiftStart = mShiftStart.getText().toString().trim();
        shiftEnd = mShiftEnd.getText().toString().trim();

        if (TextUtils.isEmpty(sDate)) {
            ToastUtil.show("Enter shift date");
            return;
        }

        if (TextUtils.isEmpty(shiftStart)) {
            ToastUtil.show("Enter shift start");
            return;
        }

        if (TextUtils.isEmpty(shiftEnd)) {
            ToastUtil.show("Enter shift end");
            return;
        }

        ProgressDialogUtil.showLoading(getContext());

        storeScheduleCall = scheduleService.storeSchedule(
                prefManager.getCompanyId(),
                getArguments().getLong("employeeId"),
                getArguments().getLong("branchId"),
                DateUtil.formattedDbDate(sDate),
                shiftStart,
                shiftEnd);

        storeScheduleCall.enqueue(new Callback<ScheduleResponse>() {
            @Override
            public void onResponse(Call<ScheduleResponse> call, Response<ScheduleResponse> response) {
                if (response.isSuccessful()) {

                    if (response.body().getError() == null) {
                        sDate = DateUtil.addDay(sDate, "dd/MM/yyyy", 1);

                        Bundle bundle = new Bundle();
                        bundle.putString("date", sDate);
                        bundle.putString("shiftStart", shiftStart);
                        bundle.putString("shiftEnd", shiftEnd);
                        bundle.putLong("employeeId", getArguments().getLong("employeeId"));
                        bundle.putString("employeeName", getArguments().getString("employeeName"));
                        bundle.putLong("branchId", getArguments().getLong("branchId"));
                        bundle.putString("branchName", getArguments().getString("branchName"));

                        FragmentHelper.replace(getActivity(), new CreateScheduleFragment(), bundle, false);
                    }
                }

                ProgressDialogUtil.dismiss();
            }

            @Override
            public void onFailure(Call<ScheduleResponse> call, Throwable t) {
                if (! call.isCanceled()) {
                    Toast.makeText(getContext(), t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }

                ProgressDialogUtil.dismiss();
            }
        });
    }

    // skip this date and go to next date
    @OnClick(R.id.no_shift)
    public void noShift() {
        sDate = mDate.getText().toString().trim();

        if (TextUtils.isEmpty(sDate)) {
            ToastUtil.show("Enter shift date");
            return;
        }

        sDate = DateUtil.addDay(sDate, "dd/MM/yyyy", 1);

        Bundle bundle = new Bundle();
        bundle.putString("date", sDate);
        bundle.putLong("employeeId", getArguments().getLong("employeeId"));
        bundle.putString("employeeName", getArguments().getString("employeeName"));
        bundle.putLong("branchId", getArguments().getLong("branchId"));
        bundle.putString("branchName", getArguments().getString("branchName"));

        FragmentHelper.replace(getActivity(), new CreateScheduleFragment(), bundle, false);
    }

    @OnClick(R.id.cv_employee)
    public void clickBtnEmployee() {
        FragmentHelper.popBackStack(getActivity(), CreateScheduleS1Fragment.class.getSimpleName());
    }

    @OnClick(R.id.cv_branch)
    public void clickBtnBranch() {
        FragmentHelper.popBackStack(getActivity(), CreateScheduleS2Fragment.class.getSimpleName());
    }
}
