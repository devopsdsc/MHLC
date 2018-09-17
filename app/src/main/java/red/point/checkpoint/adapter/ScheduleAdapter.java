package red.point.checkpoint.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import red.point.checkpoint.R;
import red.point.checkpoint.api.model.Schedule;
import red.point.checkpoint.util.DateUtil;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {

    private List<Schedule> list;

    public ScheduleAdapter(List<Schedule> list) {
        this.list = list;
    }

    @Override
    public ScheduleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ScheduleViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_schedule_list, parent, false));
    }

    @Override
    public void onBindViewHolder(final ScheduleViewHolder holder, int position) {
        Schedule schedule = list.get(position);
        holder.date.setText(DateUtil.formattedFullHumanDate(schedule.getDate()));
        holder.shift.setText(String.format("Shift: %s - %s", schedule.getShiftStart(), schedule.getShiftEnd()));
        holder.branch.setText(String.format("@ %s", schedule.getBranch().getName()));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ScheduleViewHolder extends RecyclerView.ViewHolder {
        TextView date;
        TextView shift;
        TextView branch;

        ScheduleViewHolder(View itemView) {
            super(itemView);

            date = itemView.findViewById(R.id.date);
            shift = itemView.findViewById(R.id.shift);
            branch = itemView.findViewById(R.id.branch);
        }
    }
}
