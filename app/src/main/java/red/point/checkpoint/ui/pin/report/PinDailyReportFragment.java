package red.point.checkpoint.ui.pin.report;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import red.point.checkpoint.R;
import red.point.checkpoint.adapter.PinDailyReportAdapter;
import red.point.checkpoint.api.model.Pin;
import red.point.checkpoint.api.service.EmployeeService;
import red.point.checkpoint.di.ContextModule;
import red.point.checkpoint.helper.PrefManager;
import red.point.checkpoint.util.CustomMapView;
import red.point.checkpoint.util.DateUtil;
import red.point.checkpoint.util.ProgressDialogUtil;
import red.point.checkpoint.util.RecyclerViewUtil;

/**
 * A simple {@link Fragment} subclass.
 */
public class PinDailyReportFragment extends Fragment {

    private static final String TAG = PinDailyReportFragment.class.getSimpleName();

    @Inject PrefManager prefManager;
    @Inject EmployeeService employeeService;

    @BindView(R.id.list_item) RecyclerView recyclerView;
    @BindView(R.id.employee) TextView mEmployee;
    @BindView(R.id.date) TextView mDate;
    @BindView(R.id.map_view) CustomMapView mapView;

    private Unbinder unbinder;

    private List<Pin> pins = new ArrayList<>();
    private PinDailyReportAdapter adapter;

    public PinDailyReportFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (getActivity() != null) getActivity().setTitle(R.string.drop_pin_report);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_pin_daily_report, container, false);

        unbinder = ButterKnife.bind(this, view);

        PinReportComponent pinReportComponent = DaggerPinReportComponent.builder()
                .contextModule(new ContextModule(getContext()))
                .pinReportModule(new PinReportModule())
                .build();

        pinReportComponent.injectPinDailyReport(this);

        setHasOptionsMenu(true);

        mapView.onCreate(savedInstanceState);

        mDate.setText(getArguments().getString("date"));
        mEmployee.setText(getArguments().getString("employeeName"));

        // Set adapter
        RecyclerViewUtil.setDefault(getContext(), recyclerView);
        pins = Parcels.unwrap(getArguments().getParcelable("pins"));

        for (int i=0;i<pins.size();i++) {
            Pin pin = pins.get(i);
            mapView.getMapAsync(googleMap -> {
                LatLng coordinates = new LatLng(pin.getLatitude(), pin.getLongitude());
                googleMap.addMarker(new MarkerOptions().position(coordinates).title(DateUtil.timestampToDate(pin.getCreatedAt().getTimezone(), pin.getCreatedAt().getDate())));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 16));
                googleMap.getUiSettings().setScrollGesturesEnabled(true);
                googleMap.getUiSettings().setZoomGesturesEnabled(true);
                googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.style_json));
                mapView.onResume();
            });
        }

        adapter = new PinDailyReportAdapter(pins, savedInstanceState, getContext());
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();

        ProgressDialogUtil.dismiss();
    }
}