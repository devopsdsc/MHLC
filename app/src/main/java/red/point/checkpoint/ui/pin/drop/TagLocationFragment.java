package red.point.checkpoint.ui.pin.drop;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.parceler.Parcels;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import red.point.checkpoint.R;
import red.point.checkpoint.api.model.Pin;
import red.point.checkpoint.api.model.PinLocationResponse;
import red.point.checkpoint.api.model.PinResponse;
import red.point.checkpoint.api.service.PinLocationService;
import red.point.checkpoint.api.service.PinService;
import red.point.checkpoint.di.ContextModule;
import red.point.checkpoint.helper.PrefManager;
import red.point.checkpoint.util.ProgressDialogUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class TagLocationFragment extends DialogFragment {

    private static final String TAG = TagLocationFragment.class.getSimpleName();

    @Inject PrefManager prefManager;
    @Inject PinLocationService pinLocationService;
    @Inject PinService pinService;

    @BindView(R.id.name) EditText mName;

    private Unbinder unbinder;
    private Call<PinLocationResponse> pinLocationResponseCall;
    private Call<PinResponse> pinResponseCall;
    private double latitude;
    private double longitude;
    private Pin pin;

    public TagLocationFragment() {}

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_tag_location, null);

        unbinder = ButterKnife.bind(this, view);

        DropPinComponent dropPinComponent = DaggerDropPinComponent.builder()
                .contextModule(new ContextModule(getContext()))
                .dropPinModule(new DropPinModule())
                .build();

        dropPinComponent.injectTagLocation(this);
        
        pin = Parcels.unwrap(getArguments().getParcelable("pin")); 

        builder.setPositiveButton("Save", null);
        builder.setNeutralButton("Skip Tag", (dialog, which) -> dismiss());
        builder.setCancelable(false);
        builder.setView(view);

        AlertDialog dialog = builder.create();

        latitude = getArguments().getDouble("latitude");
        longitude = getArguments().getDouble("longitude");

        dialog.setOnShowListener(dialog1 -> {
            Button positiveButton = ((AlertDialog) dialog1).getButton(DialogInterface.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {

                if (TextUtils.isEmpty(mName.getText())) {
                    Toast.makeText(getContext(), "Enter your location name", Toast.LENGTH_SHORT).show();
                    return;
                }

                ProgressDialogUtil.showLoading(getContext());

                pinLocationResponseCall = pinLocationService.storePinLocation(prefManager.getCompanyId(),
                        mName.getText().toString().trim(), "", latitude, longitude);

                pinLocationResponseCall.enqueue(new Callback<PinLocationResponse>() {
                    @Override
                    public void onResponse(Call<PinLocationResponse> call, Response<PinLocationResponse> response) {
                        if (response.isSuccessful()) {
                            updatePinTag(response.body().getPinLocation().getId());
                        }
                    }

                    @Override
                    public void onFailure(Call<PinLocationResponse> call, Throwable t) {
                        if (! call.isCanceled()) {
                            ProgressDialogUtil.dismiss();
                            dismiss();
                        }
                    }
                });
            });
        });

        return dialog;
    }

    private void updatePinTag(long pinLocationId) {
        pinResponseCall = pinService.putPin(pin.getCompanyId(),
                pin.getId(),
                pinLocationId,
                pin.getUserId(),
                pin.getAddress(),
                pin.getLatitude(),
                pin.getLongitude());
        pinResponseCall.enqueue(new Callback<PinResponse>() {
            @Override
            public void onResponse(Call<PinResponse> call, Response<PinResponse> response) {
                ProgressDialogUtil.dismiss();
                dismiss();
            }

            @Override
            public void onFailure(Call<PinResponse> call, Throwable t) {
                if (!call.isCanceled()) {
                    ProgressDialogUtil.dismiss();
                    dismiss();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unbinder.unbind();
        if (pinLocationResponseCall != null && pinLocationResponseCall.isExecuted()) pinLocationResponseCall.cancel();
        if (pinResponseCall != null && pinResponseCall.isExecuted()) pinResponseCall.cancel();
        ProgressDialogUtil.dismiss();
    }
}
