package red.point.checkpoint.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import red.point.checkpoint.R;
import red.point.checkpoint.api.model.Schedule;
import red.point.checkpoint.util.DateUtil;
import red.point.checkpoint.util.NumberUtil;

public class AttendanceReportAdapter extends RecyclerView.Adapter<AttendanceReportAdapter.ReportViewHolder> {

    private static final String TAG = AttendanceReportAdapter.class.getSimpleName();

    private List<Schedule> list;

    private double maxCharge;

    public AttendanceReportAdapter(List<Schedule> list) {
        this.list = list;
    }

    @Override
    public ReportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ReportViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_attendance_report_list, parent, false));
    }

    @Override
    public void onBindViewHolder(final ReportViewHolder holder, int position) {
        Schedule schedule = list.get(position);
        holder.branch.setText(String.format("%s", schedule.getBranch().getName()));
        holder.date.setText(DateUtil.formattedFullHumanDate(schedule.getDate()));
        holder.shift.setText(String.format("%s - %s", schedule.getShiftStart(), schedule.getShiftEnd()));
        holder.charge.setText(NumberUtil.getFormattedNumber(schedule.getCharge()));
        holder.reward.setText(NumberUtil.getFormattedNumber(schedule.getReward()));

        if (schedule.getCheckInLate() != null && schedule.getCheckInLate() > 0) {
            holder.checkInLate.setText(String.format("%s minute", schedule.getCheckInLate()));
        }

        if (schedule.getCheckOutLate() != null && schedule.getCheckOutLate() > 0) {
            holder.checkOutLate.setText(String.format("%s minute", schedule.getCheckOutLate()));
        }

        if (schedule.getCheckIn() != null) {
            holder.checkIn.setText(DateUtil.timestampToTime(schedule.getCheckIn()));
        }

        if (schedule.getCheckOut() != null) {
            holder.checkOut.setText(DateUtil.timestampToTime(schedule.getCheckOut()));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setMaxCharge(double maxCharge) {
        this.maxCharge = maxCharge;
    }

    class ReportViewHolder extends RecyclerView.ViewHolder {

        ImageView imgIcon;
        TextView branch, date, shift, checkIn, checkOut, reward, charge, checkInLate, checkOutLate;
        RelativeLayout columnCharge, columnCheckIn, columnCheckOut, columnReward, columnCheckInLate, columnCheckOutLate;

        ReportViewHolder(View itemView) {
            super(itemView);

            imgIcon = itemView.findViewById(R.id.imgIcon);
            branch = itemView.findViewById(R.id.branch);
            date = itemView.findViewById(R.id.date);
            shift = itemView.findViewById(R.id.shift);
            checkIn = itemView.findViewById(R.id.check_in);
            checkOut = itemView.findViewById(R.id.check_out);
            reward = itemView.findViewById(R.id.reward);
            charge = itemView.findViewById(R.id.charge);
            checkInLate = itemView.findViewById(R.id.check_in_late);
            checkOutLate = itemView.findViewById(R.id.check_out_late);
            columnCharge = itemView.findViewById(R.id.column_charge);
            columnCheckIn = itemView.findViewById(R.id.column_check_in);
            columnCheckOut = itemView.findViewById(R.id.column_check_out);
            columnReward = itemView.findViewById(R.id.column_reward);
            columnCheckInLate = itemView.findViewById(R.id.column_check_in_late);
            columnCheckOutLate = itemView.findViewById(R.id.column_check_out_late);
        }
    }
}
