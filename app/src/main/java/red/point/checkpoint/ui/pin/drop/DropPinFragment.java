package red.point.checkpoint.ui.pin.drop;


import android.Manifest;
import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;
import red.point.checkpoint.R;
import red.point.checkpoint.api.model.Pin;
import red.point.checkpoint.api.model.PinList;
import red.point.checkpoint.api.model.PinLocation;
import red.point.checkpoint.api.model.PinLocationList;
import red.point.checkpoint.api.model.PinResponse;
import red.point.checkpoint.api.service.PinLocationService;
import red.point.checkpoint.api.service.PinService;
import red.point.checkpoint.di.ContextModule;
import red.point.checkpoint.helper.FragmentHelper;
import red.point.checkpoint.helper.PrefManager;
import red.point.checkpoint.util.DateUtil;
import red.point.checkpoint.util.ProgressDialogUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class DropPinFragment extends Fragment implements OnMapReadyCallback,
        EasyPermissions.PermissionCallbacks {

    private static final String TAG = DropPinFragment.class.getSimpleName();

    private static final int RC_LOCATION = 1;
    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */

    @Inject
    PrefManager prefManager;

    @Inject
    PinService pinService;

    @Inject
    PinLocationService pinLocationService;

    @Inject
    PinService pinListService;

    @BindView(R.id.fab_drop_pin)
    FloatingActionButton fabDropPin;

    private Unbinder unbinder;

    private GoogleMap mGoogleMap;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private Marker mCurrLocationMarker;
    private FusedLocationProviderClient mFusedLocationClient = null;

    private Call<PinList> pinListCall;
    private Call<PinLocationList> pinLocationListCall;
    private Call<PinResponse> pinResponseCall;

    @BindView(R.id.shimmer_view_container)
    ShimmerFrameLayout shimmerFrameLayout;

    public DropPinFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_drop_pin, container, false);

        unbinder = ButterKnife.bind(this, rootView);

        DropPinComponent dropPinComponent = DaggerDropPinComponent.builder()
                .contextModule(new ContextModule(getContext()))
                .dropPinModule(new DropPinModule())
                .build();

        dropPinComponent.inject(this);

        setHasOptionsMenu(true);

        if (getActivity() != null) getActivity().setTitle(getString(R.string.nav_drop_pin));

        fabDropPin.setVisibility(View.GONE);

        // Add a shimmering effect to any view in your app.
        // It's useful as an unobtrusive loading indicator.
        shimmerFrameLayout.startShimmerAnimation();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        // SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        populateTodayPin();

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu,inflater);
        inflater.inflate(R.menu.my_pin_report, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.myPinReport :
            {
                FragmentHelper.replace(getActivity(), new MyPinReportFragment(), null, true);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onStart() {
        super.onStart();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        String[] permission = {Manifest.permission.ACCESS_FINE_LOCATION};
        if (EasyPermissions.hasPermissions(getContext(), permission)) {
            Log.d(TAG, "Request Permission Update");
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());

        } else {
            EasyPermissions.requestPermissions(
                    new PermissionRequest.Builder(this, RC_LOCATION, permission)
                            .setRationale(R.string.location_rationale)
                            .setPositiveButtonText(R.string.next)
                            .setNegativeButtonText("")
                            .build());
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        // stop location updates when Activity is no longer active
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (unbinder != null) unbinder.unbind();
        if (pinResponseCall != null && pinResponseCall.isExecuted()) pinResponseCall.cancel();
        if (pinListCall != null && pinListCall.isExecuted()) pinListCall.cancel();
        ProgressDialogUtil.dismiss();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        String[] permission = {Manifest.permission.ACCESS_FINE_LOCATION};
        if (EasyPermissions.hasPermissions(getContext(), permission)) {
            mGoogleMap.setMyLocationEnabled(true);
        } else {
            EasyPermissions.requestPermissions(
                    new PermissionRequest.Builder(this, RC_LOCATION, permission)
                            .setRationale(R.string.location_rationale)
                            .setPositiveButtonText(R.string.next)
                            .setNegativeButtonText("")
                            .build());
        }

        createMap();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        // Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @OnClick(R.id.fab_drop_pin)
    public void dropPin() {
        ProgressDialogUtil.showLoading(getContext());

        pinLocationListCall = pinLocationService.getPinLocationList(prefManager.getCompanyId(), 100, mLastLocation.getLatitude(), mLastLocation.getLongitude(), 100);
        pinLocationListCall.enqueue(new Callback<PinLocationList>() {
            @Override
            public void onResponse(Call<PinLocationList> call, Response<PinLocationList> response) {
                if (response.isSuccessful()) {
                    List<PinLocation> pinLocationList = response.body().getPinLocations();
                    List<PinLocation> pinLocationListInRadius = new ArrayList<>();

                    final ArrayAdapter<String> pinLocationAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.select_dialog_singlechoice);

                    for (int i=0;i<pinLocationList.size();i++) {
                        Location location = new Location("");
                        location.setLatitude(pinLocationList.get(i).getLatitude());
                        location.setLongitude(pinLocationList.get(i).getLongitude());

                        int distance = Math.round(mLastLocation.distanceTo(location));
                        if (distance <= 100) {
                            pinLocationListInRadius.add(pinLocationList.get(i));
                            pinLocationAdapter.add(pinLocationList.get(i).getName() + " (" + distance + "m)");
                            pinLocationAdapter.notifyDataSetChanged();
                        }
                    }

                    if (pinLocationListInRadius.size() == 0) {
                        createNewTag();
                    } else {
                        final AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());
                        builderSingle.setTitle("Tag Location");
                        builderSingle.setPositiveButton("New Tag", (dialog, which) -> createNewTag());
                        builderSingle.setAdapter(pinLocationAdapter, (dialog, which) -> saveDropPin(pinLocationListInRadius.get(which).getId()));
                        builderSingle.show();

                        ProgressDialogUtil.dismiss();
                    }
                } else {
                    ProgressDialogUtil.dismiss();
                }
            }

            @Override
            public void onFailure(Call<PinLocationList> call, Throwable t) {
                if (!call.isCanceled()) {
                    ProgressDialogUtil.dismiss();
                }
            }
        });
    }

    private void createNewTag() {
        ProgressDialogUtil.setMessage("Search available tag");
        pinResponseCall = pinService.storePin(prefManager.getCompanyId(),
                null,
                prefManager.getUserId(),
                mLastLocation.getLatitude(),
                mLastLocation.getLongitude());
        pinResponseCall.enqueue(new Callback<PinResponse>() {
            @Override
            public void onResponse(Call<PinResponse> call, Response<PinResponse> response) {
                if (response.isSuccessful()) {
                    // Place current location marker
                    LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.snippet(DateUtil.getCurrentFormattedDate() + " " + DateUtil.getCurrentTime());
                    mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);

                    // move map camera
                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));

                    Bundle bundle = new Bundle();
                    bundle.putParcelable("pin", Parcels.wrap(response.body().getPin()));
                    bundle.putDouble("latitude", mLastLocation.getLatitude());
                    bundle.putDouble("longitude", mLastLocation.getLongitude());

                    FragmentHelper.show(getActivity(), new TagLocationFragment(), bundle);
                }

                ProgressDialogUtil.dismiss();
            }

            @Override
            public void onFailure(Call<PinResponse> call, Throwable t) {
                if (! call.isCanceled()) {
                    ProgressDialogUtil.dismiss();
                }
            }
        });
    }

    private void saveDropPin(long pinLocationId) {
        ProgressDialogUtil.showLoading(getContext());
        pinResponseCall = pinService.storePin(prefManager.getCompanyId(),
                pinLocationId,
                prefManager.getUserId(),
                mLastLocation.getLatitude(),
                mLastLocation.getLongitude());
        pinResponseCall.enqueue(new Callback<PinResponse>() {
            @Override
            public void onResponse(Call<PinResponse> call, Response<PinResponse> response) {
                if (response.isSuccessful()) {
                    // Place current location marker
                    LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.snippet(DateUtil.getCurrentFormattedDate() + " " + DateUtil.getCurrentTime());
                    mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);
                    mCurrLocationMarker.showInfoWindow();
                    // move map camera
                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                }

                ProgressDialogUtil.dismiss();
            }

            @Override
            public void onFailure(Call<PinResponse> call, Throwable t) {
                Log.d(TAG, "FAILURE" + t.getLocalizedMessage());
                if (! call.isCanceled()) {
                    ProgressDialogUtil.dismiss();
                }
            }
        });
    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location location = locationResult.getLastLocation();
            Log.d(TAG, "Location : " + location.getLatitude() + ", " + location.getLongitude());
            if (location != null) {
                // The last location in the list is the newest
                mLastLocation = location;

                // move map camera
                LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));

                fabDropPin.setVisibility(View.VISIBLE);
            }
        }
    };

    @AfterPermissionGranted(RC_LOCATION)
    private void createMap() {
        shimmerFrameLayout.stopShimmerAnimation();
        shimmerFrameLayout.setVisibility(View.GONE);

        mGoogleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.style_json));
    }

    private void populateTodayPin() {
        ProgressDialogUtil.showLoading(getContext());
        pinListCall = pinListService.getPins(prefManager.getCompanyId(),
                prefManager.getUserId(),
                DateUtil.getCurrentDbDate(),
                DateUtil.getCurrentDbDate());
        pinListCall.enqueue(new Callback<PinList>() {
            @Override
            public void onResponse(Call<PinList> call, Response<PinList> response) {
                if (response.isSuccessful()) {
                    List<Pin> pins = response.body().getPins();
                    for (int i=0;i<pins.size();i++) {
                        // Place current location marker
                        LatLng latLng = new LatLng(pins.get(i).getLatitude(), pins.get(i).getLongitude());
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latLng);
                        if (pins.get(i).getPinLocation() != null) {
                            markerOptions.title(pins.get(i).getPinLocation().getName());
                        }
                        markerOptions.snippet(DateUtil.timestampToDateTime(pins.get(i).getCreatedAt().getTimezone(), pins.get(i).getCreatedAt().getDate()));
                        mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);
                        mCurrLocationMarker.showInfoWindow();

                        // move map camera
                        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                    }
                }

                ProgressDialogUtil.dismiss();
            }

            @Override
            public void onFailure(Call<PinList> call, Throwable t) {
                if (! call.isCanceled()) {
                    ProgressDialogUtil.dismiss();
                }
            }
        });
    }
}
