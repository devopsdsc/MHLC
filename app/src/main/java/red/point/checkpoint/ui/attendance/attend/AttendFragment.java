package red.point.checkpoint.ui.attendance.attend;


import android.Manifest;
import android.annotation.SuppressLint;
import android.graphics.Color;
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
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

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
import red.point.checkpoint.api.model.Schedule;
import red.point.checkpoint.api.model.ScheduleList;
import red.point.checkpoint.api.model.ScheduleResponse;
import red.point.checkpoint.api.service.ScheduleService;
import red.point.checkpoint.di.ContextModule;
import red.point.checkpoint.helper.FragmentHelper;
import red.point.checkpoint.helper.PrefManager;
import red.point.checkpoint.util.DateUtil;
import red.point.checkpoint.util.ProgressDialogUtil;
import red.point.checkpoint.util.ToastUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class AttendFragment extends Fragment implements OnMapReadyCallback,
        EasyPermissions.PermissionCallbacks {

    private static final String TAG = AttendFragment.class.getSimpleName();

    private static final int RC_LOCATION = 1;

    private Unbinder unbinder;
    private GoogleMap mGoogleMap;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private Location mScheduleLocation;
    private FusedLocationProviderClient mFusedLocationClient;
    private Schedule schedule;

    @Inject
    PrefManager prefManager;

    @Inject
    ScheduleService scheduleService;

    @Inject
    ScheduleService checkInService;

    @Inject
    ScheduleService checkOutService;

    @BindView(R.id.fab_check_in)
    FloatingActionButton fabCheckIn;

    @BindView(R.id.fab_check_out)
    FloatingActionButton fabCheckOut;

    @BindView(R.id.shimmer_view_container)
    ShimmerFrameLayout shimmerFrameLayout;

    private boolean isCameraSetup = false;

    private Call<ScheduleList> scheduleServiceCall;
    private Call<ScheduleResponse> checkInServiceCall;
    private Call<ScheduleResponse> checkOutServiceCall;

    public AttendFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_attend, container, false);

        unbinder = ButterKnife.bind(this, rootView);

        AttendComponent attendComponent = DaggerAttendComponent.builder()
                .contextModule(new ContextModule(getContext()))
                .build();

        attendComponent.inject(this);

        setHasOptionsMenu(true);

        if (getActivity() != null) getActivity().setTitle(getString(R.string.nav_attendance));

        // Add a shimmering effect to any view in your app.
        // It's useful as an unobtrusive loading indicator.
        shimmerFrameLayout.startShimmerAnimation();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        // SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu,inflater);
        inflater.inflate(R.menu.my_schedule, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mySchedule :
            {
                FragmentHelper.replace(getActivity(), new MyScheduleFragment(), null, true);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.fab_check_in)
    public void checkIn() {

        if (mLastLocation == null) {
            ToastUtil.show(getString(R.string.cannot_find_your_location));
            return;
        }

        if (mScheduleLocation == null) {
            ToastUtil.show(getString(R.string.not_in_working_hour));
        } else {
            // distance is calculated in meter
            int distance = Math.round(mLastLocation.distanceTo(mScheduleLocation));

            if (distance > 100) {
                ToastUtil.show("Check In failed, You are " + distance + "m away from location");
                return;
            }

            if (schedule.getCheckIn() != null) {
                ToastUtil.show(getString(R.string.already_checkin));
                return;
            }

            if (DateUtil.isCheckInTimeAllowed(schedule.getShiftStart())) {
                ToastUtil.show(getString(R.string.check_in_too_early));
                return;
            }

            ProgressDialogUtil.show(getContext(), "Check In...");
            checkInServiceCall = checkInService.checkIn(prefManager.getCompanyId(), schedule.getId());
            checkInServiceCall.enqueue(new Callback<ScheduleResponse>() {
                @Override
                public void onResponse(Call<ScheduleResponse> call, Response<ScheduleResponse> response) {
                    ProgressDialogUtil.dismiss();
                    if (response.isSuccessful()) {
                        schedule = response.body().getSchedule();
                        checkInSuccessDialog(response.body().getSchedule().getCheckInLate());
                    }
                }

                @Override
                public void onFailure(Call<ScheduleResponse> call, Throwable t) {
                    if (!call.isCanceled()) {
                        ProgressDialogUtil.dismiss();
                        ToastUtil.show("Check In Failed");
                        Log.d(TAG, t.getLocalizedMessage());
                    }
                }
            });
        }
    }

    @OnClick(R.id.fab_check_out)
    public void checkOut() {

        if (mLastLocation == null) {
            ToastUtil.show(getString(R.string.cannot_find_your_location));
            return;
        }

        if (mScheduleLocation == null) {
            ToastUtil.show(getString(R.string.not_in_working_hour));
        } else {
            // distance is calculated in meter
            int distance = Math.round(mLastLocation.distanceTo(mScheduleLocation));

            if (distance > 100) {
                ToastUtil.show("Check Out failed, You are " + distance + "m away from location");
                return;
            }

            if (schedule.getCheckOut() != null) {
                ToastUtil.show(getString(R.string.already_checkout));
                return;
            }

            ProgressDialogUtil.show(getContext(), "Check Out...");
            checkOutServiceCall = checkOutService.checkOut(prefManager.getCompanyId(), schedule.getId());
            checkOutServiceCall.enqueue(new Callback<ScheduleResponse>() {
                @Override
                public void onResponse(Call<ScheduleResponse> call, Response<ScheduleResponse> response) {
                    ProgressDialogUtil.dismiss();
                    if (response.isSuccessful()) {
                        schedule = response.body().getSchedule();
                        checkOutSuccessDialog(response.body().getSchedule().getCheckOutLate());
                    }
                }

                @Override
                public void onFailure(Call<ScheduleResponse> call, Throwable t) {
                    if (!call.isCanceled()) {
                        ProgressDialogUtil.dismiss();
                        ToastUtil.show("Check Out Failed");
                        Log.d(TAG, t.getLocalizedMessage());
                    }
                }
            });
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (scheduleServiceCall != null && scheduleServiceCall.isExecuted()) scheduleServiceCall.cancel();

        // stop location updates when Activity is no longer active
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (unbinder != null) unbinder.unbind();

        if (checkInServiceCall != null && checkInServiceCall.isExecuted()) checkInServiceCall.cancel();
        if (checkOutServiceCall != null && checkOutServiceCall.isExecuted()) checkOutServiceCall.cancel();

        ProgressDialogUtil.dismiss();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

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
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                isCameraSetup = true;
            }
        }
    };

    @SuppressLint("MissingPermission")
    @AfterPermissionGranted(RC_LOCATION)
    private void createMap() {
        mGoogleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.style_json));

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000 * 3);
        mLocationRequest.setFastestInterval(1000 * 3);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        String[] permission = {Manifest.permission.ACCESS_FINE_LOCATION};
        if (EasyPermissions.hasPermissions(getContext(), permission)) {
            mGoogleMap.setMyLocationEnabled(true);
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        } else {
            EasyPermissions.requestPermissions(
                    new PermissionRequest.Builder(this, RC_LOCATION, permission)
                            .setRationale(R.string.location_rationale)
                            .setPositiveButtonText(R.string.next)
                            .setNegativeButtonText("")
                            .build());
        }

        populateBranch();
    }

    private void populateBranch() {
        ProgressDialogUtil.show(getContext(), "Searching your schedule...");
        scheduleServiceCall = scheduleService.getTodaySchedulesByUser(prefManager.getCompanyId(), prefManager.getUserId(), DateUtil.getCurrentDbDate(), DateUtil.getCurrentDbDate());
        scheduleServiceCall.enqueue(new Callback<ScheduleList>() {
            @Override
            public void onResponse(Call<ScheduleList> call, Response<ScheduleList> response) {
                ProgressDialogUtil.dismiss();
                shimmerFrameLayout.stopShimmerAnimation();
                shimmerFrameLayout.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    List<Schedule> scheduleList = response.body().getSchedules();
                    for (int i = 0; i < scheduleList.size(); i++) {
                        schedule = scheduleList.get(i);
                        LatLng scheduleLocation = new LatLng(schedule.getBranch().getLatitude(), schedule.getBranch().getLongitude());
                        mScheduleLocation = new Location("");
                        mScheduleLocation.setLatitude(scheduleLocation.latitude);
                        mScheduleLocation.setLongitude(scheduleLocation.longitude);

                        Marker marker = mGoogleMap.addMarker(new MarkerOptions()
                                .position(scheduleLocation)
                                .title(schedule.getBranch().getName())
                                .snippet(schedule.getShiftStart() + " - " + schedule.getShiftEnd()));

                        mGoogleMap.addCircle(new CircleOptions()
                                .center(scheduleLocation)
                                .radius(100)
                                .strokeWidth(0)
                                .fillColor(Color.argb(20, 50, 0, 255)));

                        marker.showInfoWindow();

                        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(scheduleLocation, 16));
                    }

                    if (scheduleList.size() == 0) {
                        noScheduleToday();
                    }
                }
            }

            @Override
            public void onFailure(Call<ScheduleList> call, Throwable t) {
                if (! call.isCanceled()) {
                    ProgressDialogUtil.dismiss();
                    shimmerFrameLayout.stopShimmerAnimation();
                    shimmerFrameLayout.setVisibility(View.GONE);
                }
            }
        });
    }

    private void noScheduleToday() {
        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.schedule))
                .setMessage(getString(R.string.no_schedule))
                .setPositiveButton("Close", (dialog, whichButton) -> {})
                .show();
    }

    private void checkInSuccessDialog(long lateInMinute) {
        new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.ic_check_24dp)
                .setTitle("Check-in success")
                .setMessage(getString(R.string.checkin_success))
                .setPositiveButton("Close",
                        (dialog, whichButton) -> {

                        }
                )
                .show();
    }

    private void checkOutSuccessDialog(long lateInMinute) {
        new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.ic_check_24dp)
                .setTitle("Check-out success")
                .setMessage(getString(R.string.checkout_success))
                .setPositiveButton("Close",
                        (dialog, whichButton) -> {

                        }
                )
                .show();
    }
}
