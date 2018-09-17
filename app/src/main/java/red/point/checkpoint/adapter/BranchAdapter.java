package red.point.checkpoint.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import red.point.checkpoint.R;
import red.point.checkpoint.api.model.Branch;
import red.point.checkpoint.databinding.FragmentBranchListBinding;

public class BranchAdapter extends RecyclerView.Adapter<BranchAdapter.BranchViewHolder> {

    private static final String TAG = BranchAdapter.class.getSimpleName();
    private Context context;
    private Bundle savedInstanceState;
    private List<Branch> list;
    private FragmentBranchListBinding binding;
    private LayoutInflater layoutInflater;

    public BranchAdapter(Context context, Bundle savedInstanceState, List<Branch> list) {
        this.context = context;
        this.savedInstanceState = savedInstanceState;
        this.list = list;
    }

    @NonNull
    @Override
    public BranchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (layoutInflater == null) layoutInflater = LayoutInflater.from(parent.getContext());
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_branch_list, parent, false);
        return new BranchAdapter.BranchViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull final BranchViewHolder holder, int position) {
        final Branch branch = list.get(position);

        binding.setBranch(branch);

        holder.mapView.getMapAsync(googleMap -> {
            LatLng coordinates = new LatLng(branch.getLatitude(), branch.getLongitude());
            googleMap.addMarker(new MarkerOptions().position(coordinates).title(branch.getName()));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 16));
            googleMap.getUiSettings().setScrollGesturesEnabled(false);
            googleMap.getUiSettings().setZoomGesturesEnabled(false);
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.style_json));
            holder.mapView.onResume();
        });

        holder.mapView.onCreate(savedInstanceState);

        binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class BranchViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.map_view) MapView mapView;
        @BindView(R.id.name) TextView name;
        @BindView(R.id.address) TextView address;

        FragmentBranchListBinding itemBinding;

        BranchViewHolder(FragmentBranchListBinding itemBinding) {
            super(itemBinding.getRoot());

            this.itemBinding = itemBinding;
            ButterKnife.bind(this, itemView);
        }
    }
}
