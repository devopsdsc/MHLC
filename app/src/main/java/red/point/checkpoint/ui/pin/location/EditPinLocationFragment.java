package red.point.checkpoint.ui.pin.location;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;

import org.parceler.Parcels;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;
import red.point.checkpoint.R;
import red.point.checkpoint.api.model.PinLocation;
import red.point.checkpoint.api.model.PinLocationResponse;
import red.point.checkpoint.api.service.PinLocationService;
import red.point.checkpoint.di.ContextModule;
import red.point.checkpoint.helper.FragmentHelper;
import red.point.checkpoint.helper.PrefManager;
import red.point.checkpoint.util.ProgressDialogUtil;
import red.point.checkpoint.util.ToastUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditPinLocationFragment extends DialogFragment implements EasyPermissions.PermissionCallbacks {

    private static final String TAG = EditPinLocationFragment.class.getSimpleName();
    private static final int RC_LOCATION = 4001;

    @Inject PrefManager prefManager;
    @Inject PinLocationService pinLocationService;

    @BindView(R.id.name) EditText mName;
    @BindView(R.id.address) EditText mAddress;
    @BindView(R.id.map_view) MapView mapView;

    private Unbinder unbinder;
    private GoogleMap gMap;
    private Button positiveButton;
    private Button negativeButton;
    private Call<PinLocationResponse> editPinLocationResponseCall;
    private PinLocation pinLocation;

    public EditPinLocationFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View rootView = getActivity().getLayoutInflater().inflate(R.layout.fragment_edit_pin_location, null);

        unbinder = ButterKnife.bind(this, rootView);

        EditPinLocationComponent editPinLocationComponent = DaggerEditPinLocationComponent.builder()
                .contextModule(new ContextModule(getContext()))
                .editPinLocationModule(new EditPinLocationModule())
                .build();

        editPinLocationComponent.inject(this);

        pinLocation = Parcels.unwrap(getArguments().getParcelable("pinLocation"));

        builder.setPositiveButton("Save", null);
        builder.setNegativeButton("Cancel", null);
        builder.setView(rootView);

        mName.setText(pinLocation.getName());
        mAddress.setText(pinLocation.getAddress());

        createMap(rootView, savedInstanceState);

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialog1 -> {
            positiveButton = ((AlertDialog) dialog1).getButton(DialogInterface.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {

                if (TextUtils.isEmpty(mName.getText().toString().trim())) {
                    Toast.makeText(getContext(), "Enter your pin location name", Toast.LENGTH_SHORT).show();
                    return;
                }

                ProgressDialogUtil.showLoading(getContext());
                editPinLocationResponseCall = pinLocationService.putPinLocation(
                        prefManager.getCompanyId(),
                        pinLocation.getId(),
                        mName.getText().toString().trim(),
                        mAddress.getText().toString().trim(),
                        pinLocation.getLatitude(),
                        pinLocation.getLongitude());

                editPinLocationResponseCall.enqueue(new Callback<PinLocationResponse>() {
                    @Override
                    public void onResponse(Call<PinLocationResponse> call, Response<PinLocationResponse> response) {
                        ProgressDialogUtil.dismiss();
                        if (response.isSuccessful()) {
                            ToastUtil.show("PinLocation updated");

                            pinLocation = response.body().getPinLocation();

                            Bundle bundle = new Bundle();
                            bundle.putParcelable("pinLocation", Parcels.wrap(pinLocation));

                            FragmentHelper.replace(Objects.requireNonNull(getActivity()), new PinLocationFragment(), bundle, false);
                        }

                        dismiss();
                    }

                    @Override
                    public void onFailure(Call<PinLocationResponse> call, Throwable t) {
                        if (! call.isCanceled()) {
                            ToastUtil.show(t.getLocalizedMessage());
                            ProgressDialogUtil.dismiss();
                        }

                        dismiss();
                    }
                });
            });

            negativeButton = ((AlertDialog) dialog1).getButton(DialogInterface.BUTTON_NEGATIVE);
            negativeButton.setText(R.string.cancel);
            negativeButton.setOnClickListener(v -> dismiss());
        });

        return dialog;
    }

    @SuppressLint("MissingPermission")
    private void createMap(View rootView, Bundle savedInstanceState) {
        mapView.getMapAsync((GoogleMap googleMap) -> {
            gMap = googleMap;
            final LatLng coordinates = new LatLng(pinLocation.getLatitude(), pinLocation.getLongitude());
            googleMap.addMarker(new MarkerOptions().position(coordinates).title(pinLocation.getName()));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 16));
            String[] permission = {Manifest.permission.ACCESS_FINE_LOCATION};
            if (EasyPermissions.hasPermissions(getContext(), permission)) {
                googleMap.setMyLocationEnabled(true);
            } else {
                EasyPermissions.requestPermissions(
                        new PermissionRequest.Builder(this, RC_LOCATION, permission)
                                .setRationale(R.string.location_rationale)
                                .setPositiveButtonText(R.string.next)
                                .setNegativeButtonText("")
                                .build());
            }
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.style_json));

            mapView.onResume();
        });
        mapView.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();

        if (editPinLocationResponseCall != null && editPinLocationResponseCall.isExecuted()) editPinLocationResponseCall.cancel();

        ProgressDialogUtil.dismiss();

        super.onDestroyView();
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        getActivity().recreate();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        // Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
}
