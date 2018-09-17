package red.point.checkpoint.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import red.point.checkpoint.R;
import red.point.checkpoint.api.model.PinLocation;

public class PinLocationAdapter extends RecyclerView.Adapter<PinLocationAdapter.PinLocationViewHolder> {

    private static final String TAG = PinLocationAdapter.class.getSimpleName();

    private List<PinLocation> list;

    public PinLocationAdapter(List<PinLocation> list) {
        this.list = list;
    }

    @Override
    public PinLocationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PinLocationViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_pin_location_list, parent, false));
    }


    @Override
    public void onBindViewHolder(PinLocationViewHolder holder, int position) {
        PinLocation pinLocation = list.get(position);
        holder.name.setText(pinLocation.getName());
        holder.address.setText(pinLocation.getAddress());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class PinLocationViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.name) TextView name;
        @BindView(R.id.address) TextView address;

        PinLocationViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}
