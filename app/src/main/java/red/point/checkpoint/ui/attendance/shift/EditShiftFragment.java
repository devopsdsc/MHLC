package red.point.checkpoint.ui.attendance.shift;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.parceler.Parcels;

import java.util.Calendar;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import red.point.checkpoint.R;
import red.point.checkpoint.api.model.Shift;
import red.point.checkpoint.api.model.ShiftResponse;
import red.point.checkpoint.api.service.ShiftService;
import red.point.checkpoint.di.ContextModule;
import red.point.checkpoint.helper.FragmentHelper;
import red.point.checkpoint.helper.PrefManager;
import red.point.checkpoint.util.ProgressDialogUtil;
import red.point.checkpoint.util.ToastUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditShiftFragment extends DialogFragment {

    @Inject PrefManager prefManager;
    @Inject ShiftService shiftService;

    @BindView(R.id.name) EditText mName;
    @BindView(R.id.shift_start) EditText mShiftStart;
    @BindView(R.id.shift_end) EditText mShiftEnd;

    private Unbinder unbinder;
    private Call<ShiftResponse> shiftCall;

    public EditShiftFragment() {}

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_edit_shift, null);

        unbinder = ButterKnife.bind(this, view);

        ShiftComponent shiftComponent = DaggerShiftComponent.builder()
                .contextModule(new ContextModule(getContext()))
                .shiftModule(new ShiftModule())
                .build();

        shiftComponent.inject(this);

        builder.setPositiveButton("Save", null);
        builder.setNegativeButton("Cancel", (dialog, which) -> dismiss());
        builder.setView(view);

        Shift shift = Parcels.unwrap(getArguments().getParcelable("shift"));

        mName.setText(shift.getName());
        mShiftStart.setText(shift.getShiftStart());
        mShiftEnd.setText(shift.getShiftEnd());

        mShiftStart.setFocusable(false);
        mShiftStart.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);
            TimePickerDialog mTimePicker;
            mTimePicker = new TimePickerDialog(getActivity(), (timePicker, selectedHour, selectedMinute) -> mShiftStart.setText(String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)), hour, minute, true);
            mTimePicker.setTitle("Select Time");
            mTimePicker.show();
        });

        mShiftEnd.setFocusable(false);
        mShiftEnd.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);
            TimePickerDialog mTimePicker;
            mTimePicker = new TimePickerDialog(getActivity(), (timePicker, selectedHour, selectedMinute) -> mShiftEnd.setText(String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)), hour, minute, true);
            mTimePicker.setTitle("Select Time");
            mTimePicker.show();
        });

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialog1 -> {
            Button positiveButton = ((AlertDialog) dialog1).getButton(DialogInterface.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                ProgressDialogUtil.showLoading(getContext());

                String name = mName.getText().toString().trim();
                String shiftStart = mShiftStart.getText().toString().trim();
                String shiftEnd = mShiftEnd.getText().toString().trim();

                if (TextUtils.isEmpty(name)) {
                    ToastUtil.show("Enter shift name");
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

                shiftCall = shiftService.putShift(prefManager.getCompanyId(), shift.getId(), name, shiftStart, shiftEnd);
                shiftCall.enqueue(new Callback<ShiftResponse>() {
                    @Override
                    public void onResponse(Call<ShiftResponse> call, Response<ShiftResponse> response) {
                        if (response.isSuccessful()) {
                            ToastUtil.show("Shift updated");

                            FragmentHelper.replace(getActivity(), new ShiftFragment(), null, false);
                        }

                        ProgressDialogUtil.dismiss();
                        dismiss();
                    }

                    @Override
                    public void onFailure(Call<ShiftResponse> call, Throwable t) {
                        if (! call.isCanceled()) {
                            ToastUtil.show(t.getLocalizedMessage());
                            ProgressDialogUtil.dismiss();
                        }

                        dismiss();
                    }
                });
            });
        });

        return dialog;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unbinder.unbind();
        if (shiftCall != null && shiftCall.isExecuted()) shiftCall.cancel();
    }
}
