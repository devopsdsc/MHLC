package red.point.checkpoint.ui.attendance.branch;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;
import red.point.checkpoint.R;
import red.point.checkpoint.api.model.Branch;
import red.point.checkpoint.api.model.BranchResponse;
import red.point.checkpoint.api.service.BranchService;
import red.point.checkpoint.di.ContextModule;
import red.point.checkpoint.helper.FragmentHelper;
import red.point.checkpoint.helper.PrefManager;
import red.point.checkpoint.util.ProgressDialogUtil;
import red.point.checkpoint.util.ToastUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class CreateBranchFragment extends DialogFragment implements EasyPermissions.PermissionCallbacks {

    private static final String TAG = CreateBranchFragment.class.getSimpleName();
    private static final int RC_LOCATION = 4001;

    @Inject PrefManager prefManager;
    @Inject BranchService branchService;

    @BindView(R.id.name) EditText mName;
    @BindView(R.id.address) EditText mAddress;
    @BindView(R.id.latitude) EditText mLatitude;
    @BindView(R.id.longitude) EditText mLongitude;
    @BindView(R.id.map_view) MapView mapView;

    private Unbinder unbinder;
    private GoogleMap gMap;
    private SupportPlaceAutocompleteFragment autocompleteFragment;
    private Button positiveButton;
    private Button negativeButton;
    private Call<BranchResponse> createBranchResponseCall;
    private Branch branch;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private boolean isCameraSetup = false;
    private AlertDialog dialog;

    public CreateBranchFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View rootView = getActivity().getLayoutInflater().inflate(R.layout.fragment_create_branch, null);

        builder.setPositiveButton("Save", null);
        builder.setNegativeButton("Cancel", null);
        builder.setView(rootView);

        AlertDialog dialog = builder.create();

        unbinder = ButterKnife.bind(this, rootView);

        CreateBranchComponent createBranchComponent = DaggerCreateBranchComponent.builder()
                .contextModule(new ContextModule(getContext()))
                .createBranchModule(new CreateBranchModule())
                .build();

        createBranchComponent.inject(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        createMap(rootView, savedInstanceState);

        dialog.setOnShowListener(dialog1 -> {
            positiveButton = ((AlertDialog) dialog1).getButton(DialogInterface.BUTTON_POSITIVE);
            negativeButton = ((AlertDialog) dialog1).getButton(DialogInterface.BUTTON_NEGATIVE);
            negativeButton.setText(R.string.cancel);
            positiveButton.setOnClickListener(v -> submitCreateBranch());
            negativeButton.setOnClickListener(v -> closeDialog());
        });

        ProgressDialogUtil.dismiss();

        return dialog;
    }

    private void closeDialog() {
        FragmentHelper.replace(getActivity(), new BranchFragment(), null, false);
        dismiss();
    }

    @SuppressLint("MissingPermission")
    private void createMap(View rootView, Bundle savedInstanceState) {
        mapView.getMapAsync((GoogleMap googleMap) -> {
            gMap = googleMap;

            String[] permission = {Manifest.permission.ACCESS_FINE_LOCATION};
            if (EasyPermissions.hasPermissions(getContext(), permission)) {
                googleMap.setMyLocationEnabled(true);

                mLocationRequest = new LocationRequest();
                mLocationRequest.setInterval(1000 * 3);
                mLocationRequest.setFastestInterval(1000 * 3);
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            } else {
                EasyPermissions.requestPermissions(
                        new PermissionRequest.Builder(this, RC_LOCATION, permission)
                                .setRationale(R.string.location_rationale)
                                .setPositiveButtonText(R.string.next)
                                .setNegativeButtonText("")
                                .build());
            }
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.style_json));
            googleMap.setOnMapClickListener(point -> {
                googleMap.clear();
                googleMap.addMarker(new MarkerOptions().position(point));
                mLatitude.setText(String.format("%s", point.latitude));
                mLongitude.setText(String.format("%s", point.longitude));

                googleMap.addCircle(new CircleOptions()
                        .center(new LatLng(point.latitude, point.longitude))
                        .radius(100)
                        .strokeWidth(0)
                        .fillColor(Color.argb(20, 50, 0, 255)));
            });
            mapView.onResume();
        });
        mapView.onCreate(savedInstanceState);

        autocompleteFragment = (SupportPlaceAutocompleteFragment) getActivity()
                .getSupportFragmentManager()
                .findFragmentById(R.id.place_autocomplete_fragment);

        rootView.findViewById(R.id.place_autocomplete_search_input).setPadding(0,0,0,0);
        rootView.findViewById(R.id.place_autocomplete_fragment).setBackgroundColor(Color.WHITE);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                mAddress.setText(String.format("%s", place.getAddress()));
                mLatitude.setText(String.format("%s", place.getLatLng().latitude));
                mLongitude.setText(String.format("%s", place.getLatLng().longitude));

                final LatLng coordinates = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);
                gMap.addMarker(new MarkerOptions().position(coordinates));
                gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 16));

                gMap.addCircle(new CircleOptions()
                        .center(coordinates)
                        .radius(100)
                        .strokeWidth(0)
                        .fillColor(Color.argb(20, 50, 0, 255)));
            }

            @Override
            public void onError(Status status) {
                Log.e(TAG, "An error occurred: " + status);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();

        // stop location updates when Activity is no longer active
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    public void onDestroyView() {
        if (autocompleteFragment != null) {
            autocompleteFragment.setOnPlaceSelectedListener(null);
            if (getActivity() != null) {
                Fragment fragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.remove(fragment);
                ft.commitAllowingStateLoss();
            }
        }

        unbinder.unbind();

        if (createBranchResponseCall != null && createBranchResponseCall.isExecuted()) createBranchResponseCall.cancel();

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

    private void submitCreateBranch() {
        if (TextUtils.isEmpty(mName.getText())) {
            Toast.makeText(getContext(), "Enter your branch name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(mAddress.getText())) {
            Toast.makeText(getContext(), "Enter your branch address", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(mLatitude.getText()) || TextUtils.isEmpty(mLongitude.getText())) {
            Toast.makeText(getContext(), "Enter your branch address", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialogUtil.showLoading(getContext());

        Branch branch = new Branch();
        branch.setName(mName.getText().toString().trim());
        branch.setAddress(mAddress.getText().toString().trim());
        branch.setLatitude(Double.parseDouble(mLatitude.getText().toString().trim()));
        branch.setLongitude(Double.parseDouble(mLongitude.getText().toString().trim()));

        createBranchResponseCall = branchService.storeBranch(prefManager.getCompanyId(),
                branch.getName(), branch.getAddress(), branch.getLatitude(), branch.getLongitude());

        createBranchResponseCall.enqueue(new Callback<BranchResponse>() {
            @Override
            public void onResponse(Call<BranchResponse> call, Response<BranchResponse> response) {
                if (response.isSuccessful()) {
                    FragmentHelper.replace(getActivity(), new BranchFragment(), null, false);
                }

                ProgressDialogUtil.dismiss();

                dismiss();
            }

            @Override
            public void onFailure(Call<BranchResponse> call, Throwable t) {
                if (! call.isCanceled()) {
                    ProgressDialogUtil.dismiss();
                    ToastUtil.show(t.getLocalizedMessage());
                    dismiss();
                }
            }
        });
    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();

            if (locationList.size() > 0) {
                // The last location in the list is the newest
                mLastLocation = locationList.get(locationList.size() - 1);
            }

            // move map camera
            if (mLastLocation != null && !isCameraSetup) {
                LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

                mLatitude.setText(String.format("%s", mLastLocation.getLatitude()));
                mLongitude.setText(String.format("%s", mLastLocation.getLongitude()));

                gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                isCameraSetup = true;
            }
        }
    };
}
