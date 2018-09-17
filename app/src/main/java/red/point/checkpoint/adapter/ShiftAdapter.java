package red.point.checkpoint.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import red.point.checkpoint.R;
import red.point.checkpoint.api.model.Shift;

public class ShiftAdapter extends RecyclerView.Adapter<ShiftAdapter.ShiftViewHolder> {

    private List<Shift> list;

    public ShiftAdapter(List<Shift> list) {
        this.list = list;
    }

    @Override
    public ShiftViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ShiftViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_shift_list, parent, false));
    }

    @Override
    public void onBindViewHolder(ShiftViewHolder holder, int position) {
        Shift shift = list.get(position);
        holder.name.setText(shift.getName());
        holder.shift.setText(String.format("%s - %s", shift.getShiftStart(), shift.getShiftEnd()));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ShiftViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView shift;

        ShiftViewHolder(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            shift = itemView.findViewById(R.id.shift);
        }
    }
}
