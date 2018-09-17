package red.point.checkpoint.ui.pin.location;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;
import red.point.checkpoint.R;
import red.point.checkpoint.adapter.PinLocationAdapter;
import red.point.checkpoint.api.model.PinLocation;
import red.point.checkpoint.api.model.PinLocationList;
import red.point.checkpoint.api.model.PinLocationResponse;
import red.point.checkpoint.api.service.PinLocationService;
import red.point.checkpoint.di.ContextModule;
import red.point.checkpoint.helper.FragmentHelper;
import red.point.checkpoint.helper.PrefManager;
import red.point.checkpoint.helper.RecyclerItemClickListener;
import red.point.checkpoint.ui.MainFragment;
import red.point.checkpoint.ui.attendance.branch.BranchFragment;
import red.point.checkpoint.util.ProgressDialogUtil;
import red.point.checkpoint.util.RecyclerViewUtil;
import red.point.checkpoint.util.ToastUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class PinLocationFragment extends MainFragment implements OnMapReadyCallback,
        EasyPermissions.PermissionCallbacks {

    private static final String TAG = PinLocationFragment.class.getSimpleName();

    @Inject
    PinLocationAdapter pinLocationAdapter;

    @Inject
    PrefManager prefManager;

    @Inject
    PinLocationService pinLocationService;

    @Inject
    PinLocationService deletePinLocationService;

    private List<PinLocation> listPinLocation = new ArrayList<>();
    private Call<PinLocationList> pinLocationListCall;
    private Call<PinLocationResponse> pinResponseCall;
    private static final int RC_LOCATION = 1;
    private Location mLastLocation;
    private Unbinder unbinder;

    private List<Marker> markers = new ArrayList<Marker>();

    @BindView(R.id.fabAdd) FloatingActionButton addButton;
    @BindView(R.id.emptyLayout) RelativeLayout emptyLayout;
    @BindView(R.id.emptyIcon) ImageView emptyIcon;
    @BindView(R.id.emptyText) TextView emptyText;
    @BindView(R.id.item_list) RecyclerView recyclerView;
    @BindView(R.id.shimmer_view_container) ShimmerFrameLayout shimmerFrameLayout;
    @BindView(R.id.map_view) MapView mapView;
    @BindView(R.id.content) LinearLayout content;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;

    public PinLocationFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (getActivity() != null) getActivity().setTitle(getString(R.string.pin_location));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pin_location, container, false);

        PinLocationComponent pinLocationComponent = DaggerPinLocationComponent.builder()
                .pinLocationModule(new PinLocationModule(listPinLocation))
                .contextModule(new ContextModule(getContext()))
                .build();

        pinLocationComponent.inject(this);

        unbinder = ButterKnife.bind(this, view);

        // Add a shimmering effect to any view in your app.
        // It's useful as an unobtrusive loading indicator.
        shimmerFrameLayout.startShimmerAnimation();

        // Add empty layout when no data fetched
        emptyLayout.setVisibility(View.GONE);
        emptyText.setText(R.string.no_pin_location_tag);

        // Initiate recycler view
        RecyclerViewUtil.setDefault(getContext(), recyclerView);

        // Set recycler view adapter
        recyclerView.addOnItemTouchListener(recyclerItemClickListener);
        recyclerView.setAdapter(pinLocationAdapter);

        mapView.onCreate(savedInstanceState);

        return view;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onStart() {
        super.onStart();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setSmallestDisplacement(100);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null) unbinder.unbind();
        ProgressDialogUtil.dismiss();
    }

    private void updateView() {
        if (listPinLocation.size() == 0) {
            content.setVisibility(View.GONE);
            emptyLayout.setVisibility(View.VISIBLE);
        } else {
            content.setVisibility(View.VISIBLE);
            emptyLayout.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.fabAdd)
    void addPinLocation() {
        addButton.setEnabled(false);

        FragmentHelper.show(getActivity(), new CreatePinLocationFragment(), null);
    }

    private RecyclerItemClickListener recyclerItemClickListener = new RecyclerItemClickListener(getContext(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
        @Override public void onItemClick(View view, int position) {
            mapView.getMapAsync(googleMap -> {
                Log.d(TAG, listPinLocation.get(position).getName());
                LatLng coordinates = new LatLng(listPinLocation.get(position).getLatitude(), listPinLocation.get(position).getLongitude());
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 16));
                markers.get(position).showInfoWindow();
                mapView.onResume();
            });
        }

        @Override public void onLongItemClick(View view, int position) {
            AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
            builder.setTitle(listPinLocation.get(position).getName())
                    .setPositiveButton("Edit", (dialog, which) -> editPinLocation(listPinLocation.get(position)))
                    .setNeutralButton("Delete", (dialog, which) -> deletePinLocation(position))
                    .show();
        }
    });

    private void editPinLocation(PinLocation pinLocation) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("pinLocation", Parcels.wrap(pinLocation));

        FragmentHelper.show(getActivity(), new EditPinLocationFragment(), bundle);
    }

    private void deletePinLocation(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
        builder.setTitle("Delete Pin Location")
                .setMessage("Are you sure you want to delete this location tag ?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> deletePinLocationConfirmed(position))
                .setNegativeButton(android.R.string.no, (dialog, which) -> {})
                .setIcon(R.drawable.ic_warning_24dp)
                .show();
    }

    private void deletePinLocationConfirmed(int position) {
        ProgressDialogUtil.showLoading(getContext());

        pinResponseCall = pinLocationService.deletePinLocation(prefManager.getCompanyId(), listPinLocation.get(position).getId());
        pinResponseCall.enqueue(new Callback<PinLocationResponse>() {
            @Override
            public void onResponse(Call<PinLocationResponse> call, Response<PinLocationResponse> response) {
                ProgressDialogUtil.dismiss();
                if (response.isSuccessful()) {
                    ToastUtil.show("Pin Location deleted");

                    listPinLocation.remove(position);
                    pinLocationAdapter.notifyDataSetChanged();

                    FragmentHelper.popBackStack(getActivity(), BranchFragment.class.getSimpleName());
                }
            }

            @Override
            public void onFailure(Call<PinLocationResponse> call, Throwable t) {
                if (! call.isCanceled()) {
                    ProgressDialogUtil.dismiss();
                    ToastUtil.show("Connection Failure");
                }
            }
        });
    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            mLastLocation = locationResult.getLastLocation();
            pinLocationListCall = pinLocationService.getPinLocationList(prefManager.getCompanyId(), 50, mLastLocation.getLatitude(),mLastLocation.getLongitude(), 1000);
            pinLocationListCall.enqueue(new Callback<PinLocationList>() {
                @Override
                public void onResponse(Call<PinLocationList> call, Response<PinLocationList> response) {
                    if (response.isSuccessful()) {
                        List<PinLocation> pinLocations = response.body().getPinLocations();
                        listPinLocation.clear();
                        for (int i = 0; i < pinLocations.size(); i++) {
                            PinLocation pinLocation = pinLocations.get(i);
                            listPinLocation.add(pinLocation);
                            pinLocationAdapter.notifyDataSetChanged();

                            mapView.getMapAsync(googleMap -> {
                                Log.d(TAG, pinLocation.getName());
                                LatLng coordinates = new LatLng(pinLocation.getLatitude(), pinLocation.getLongitude());
                                Marker marker = googleMap.addMarker(new MarkerOptions().position(coordinates).title(pinLocation.getName()));
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 16));
                                googleMap.getUiSettings().setScrollGesturesEnabled(true);
                                googleMap.getUiSettings().setZoomGesturesEnabled(true);
                                googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.style_json));
                                marker.showInfoWindow();

                                markers.add(marker);

                                mapView.onResume();
                            });
                        }

                        shimmerFrameLayout.setVisibility(View.GONE);
                        shimmerFrameLayout.stopShimmerAnimation();
                        content.setVisibility(View.VISIBLE);
                        updateView();
                    }
                }

                @Override
                public void onFailure(Call<PinLocationList> call, Throwable t) {

                }
            });
        }
    };

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
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
    }
}
