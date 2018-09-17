package red.point.checkpoint.ui.notification;


import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import red.point.checkpoint.adapter.NotificationAdapter;
import red.point.checkpoint.api.model.Notification;
import red.point.checkpoint.api.model.NotificationList;
import red.point.checkpoint.api.model.NotificationResponse;
import red.point.checkpoint.api.service.NotificationService;
import red.point.checkpoint.di.ContextModule;
import red.point.checkpoint.helper.FragmentHelper;
import red.point.checkpoint.helper.PrefManager;
import red.point.checkpoint.helper.RecyclerItemClickListener;
import red.point.checkpoint.ui.employee.EmployeeFragment;
import red.point.checkpoint.util.DateUtil;
import red.point.checkpoint.util.ProgressDialogUtil;
import red.point.checkpoint.util.RecyclerViewUtil;
import red.point.checkpoint.util.ToastUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class NotificationFragment extends Fragment {

    private static final String TAG = EmployeeFragment.class.getSimpleName();

    @Inject
    PrefManager prefManager;
    @Inject
    NotificationService notificationService;

    Unbinder unbinder;

    @BindView(R.id.date) EditText mDate;
    @BindView(R.id.time) EditText mTime;
    @BindView(R.id.message) EditText mMessage;
    @BindView(R.id.list_item)
    RecyclerView recyclerView;

    @BindView(R.id.shimmer_view_container)
    ShimmerFrameLayout shimmerFrameLayout;

    String sMessage;
    String sDate;
    String sTime;

    private Call<NotificationList> notificationListCall;
    private Call<NotificationResponse> storeNotificationCall;
    private NotificationAdapter adapter;
    private List<Notification> result = new ArrayList<>();

    public NotificationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        unbinder = ButterKnife.bind(this, view);

        if (getActivity() != null) getActivity().setTitle("Notification");

        NotificationComponent notificationComponent = DaggerNotificationComponent.builder()
                .contextModule(new ContextModule(getContext()))
                .notificationModule(new NotificationModule())
                .build();

        notificationComponent.inject(this);

        // Initiate recycler view
        RecyclerViewUtil.setDefault(getContext(), recyclerView);

        // Set adapter to recycler view
        adapter = new NotificationAdapter(result);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnItemTouchListener(NotificationClickListener);

        // Load data
        shimmerFrameLayout.startShimmerAnimation();
        notificationListCall = notificationService.getNotifications(prefManager.getCompanyId());
        notificationListCall.enqueue(new Callback<NotificationList>() {
            @Override
            public void onResponse(Call<NotificationList> call, Response<NotificationList> response) {
                if (response.isSuccessful()) {
                    List<Notification> notifications = Objects.requireNonNull(response.body()).getNotifications();
                    result.clear();
                    result.addAll(notifications);
                    adapter.notifyDataSetChanged();
                }
                shimmerFrameLayout.setVisibility(View.GONE);
                shimmerFrameLayout.stopShimmerAnimation();
            }

            @Override
            public void onFailure(Call<NotificationList> call, Throwable t) {
                if (! call.isCanceled()) {
                    shimmerFrameLayout.setVisibility(View.GONE);
                    shimmerFrameLayout.stopShimmerAnimation();
                    Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    RecyclerItemClickListener NotificationClickListener = new RecyclerItemClickListener(getContext(), recyclerView ,new RecyclerItemClickListener.OnItemClickListener() {
        @Override public void onItemClick(View view, int position) {
            // TODO: add option to delete notification
        }

        @Override public void onLongItemClick(View view, int position) {
            // do whatever
        }
    });

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

    @OnClick(R.id.time)
    public void chooseTime() {
        Calendar mCurrentTime = Calendar.getInstance();
        int hour = mCurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mCurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(getActivity(), (timePicker, selectedHour, selectedMinute) ->
                mTime.setText(String.format(Locale.getDefault(),
                        "%02d:%02d", selectedHour, selectedMinute)), hour, minute, true);
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }

    @OnClick(R.id.add)
    public void save() {
        sMessage = mMessage.getText().toString().trim();
        sDate = mDate.getText().toString().trim();
        sTime = mTime.getText().toString().trim();

        if (TextUtils.isEmpty(sDate)) {
            ToastUtil.show("Enter notification date");
            return;
        }

        if (TextUtils.isEmpty(sTime)) {
            ToastUtil.show("Enter notification time");
            return;
        }

        if (TextUtils.isEmpty(sMessage)) {
            ToastUtil.show("Enter notification message");
            return;
        }

        ProgressDialogUtil.showLoading(getContext());

        storeNotificationCall = notificationService.storeNotification(
                prefManager.getCompanyId(),
                prefManager.getUserId(),
                DateUtil.formattedDbDate(sDate),
                sTime,
                sMessage);

        storeNotificationCall.enqueue(new Callback<NotificationResponse>() {
            @Override
            public void onResponse(Call<NotificationResponse> call, Response<NotificationResponse> response) {
                if (response.isSuccessful()) {

                    if (response.body().getError() == null) {
                        sDate = DateUtil.addDay(sDate, "dd/MM/yyyy", 1);

                        FragmentHelper.replace(getActivity(), new NotificationFragment(), null, false);
                    }
                }

                ProgressDialogUtil.dismiss();
            }

            @Override
            public void onFailure(Call<NotificationResponse> call, Throwable t) {
                if (! call.isCanceled()) {
                    Toast.makeText(getContext(), t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }

                ProgressDialogUtil.dismiss();
            }
        });
    }

}
