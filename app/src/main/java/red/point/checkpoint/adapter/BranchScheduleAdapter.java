package red.point.checkpoint.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import red.point.checkpoint.R;
import red.point.checkpoint.api.model.Schedule;
import red.point.checkpoint.ui.attendance.branch.BranchScheduleComponent;

public class BranchScheduleAdapter extends RecyclerView.Adapter<BranchScheduleAdapter.BranchScheduleViewHolder> {

    private static final String TAG = BranchScheduleComponent.class.getSimpleName();

    private List<Schedule> list;

    public BranchScheduleAdapter(List<Schedule> list) {
        this.list = list;
    }

    @Override
    public BranchScheduleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new BranchScheduleViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_branch_schedule_list, parent, false));
    }

    @Override
    public void onBindViewHolder(final BranchScheduleViewHolder holder, int position) {
        Schedule branchSchedule = list.get(position);
        holder.name.setText(branchSchedule.getEmployee().getName());
        holder.shift.setText(String.format("Shift: %s - %s", branchSchedule.getShiftStart(), branchSchedule.getShiftEnd()));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class BranchScheduleViewHolder extends RecyclerView.ViewHolder {
        TextView name, shift;

        BranchScheduleViewHolder(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            shift = itemView.findViewById(R.id.shift);
        }
    }
}
