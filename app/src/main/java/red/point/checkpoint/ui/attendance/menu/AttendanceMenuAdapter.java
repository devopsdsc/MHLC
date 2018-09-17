package red.point.checkpoint.ui.attendance.menu;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import red.point.checkpoint.R;
import red.point.checkpoint.helper.FragmentHelper;
import red.point.checkpoint.ui.attendance.branch.BranchFragment;
import red.point.checkpoint.ui.attendance.report.AttendanceReportFragment;
import red.point.checkpoint.ui.attendance.schedule.ScheduleFragment;
import red.point.checkpoint.ui.attendance.shift.ShiftFragment;
import red.point.checkpoint.ui.company.CompanyFragment;
import red.point.checkpoint.ui.employee.EmployeeFragment;
import red.point.checkpoint.ui.notification.NotificationFragment;
import red.point.checkpoint.ui.setting.SettingFragment;
import red.point.checkpoint.ui.wallet.WalletFragment;

public class AttendanceMenuAdapter extends RecyclerView.Adapter<AttendanceMenuAdapter.AttendanceMenuViewHolder> {

    private static final String TAG = AttendanceMenuAdapter.class.getSimpleName();
    private FragmentActivity context;
    private List<AttendanceMenu> list;

    AttendanceMenuAdapter(FragmentActivity context, List<AttendanceMenu> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public AttendanceMenuViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AttendanceMenuViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_attendance_menu_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceMenuViewHolder holder, int position) {
        AttendanceMenu generalMenu = list.get(position);
        holder.image.setImageResource(generalMenu.getImage());
        holder.image.setColorFilter(ContextCompat.getColor(context, generalMenu.getColor()));
        holder.title.setText(generalMenu.getTitle());

        holder.itemView.setOnClickListener(v -> {
            if (generalMenu.getTitle().equals(context.getString(R.string.menu_company))) {
                FragmentHelper.replace(context, new CompanyFragment(), null, true);
            } else if (generalMenu.getTitle().equals(context.getString(R.string.menu_wallet))) {
                FragmentHelper.replace(context, new WalletFragment(), null, true);
            } else if (generalMenu.getTitle().equals(context.getString(R.string.menu_employee))) {
                FragmentHelper.replace(context, new EmployeeFragment(), null, true);
            } else if (generalMenu.getTitle().equals(context.getString(R.string.menu_setting))) {
                FragmentHelper.replace(context, new SettingFragment(), null, true);
            } else if (generalMenu.getTitle().equals(context.getString(R.string.menu_branch))) {
                FragmentHelper.replace(context, new BranchFragment(), null, true);
            } else if (generalMenu.getTitle().equals(context.getString(R.string.menu_shift))) {
                FragmentHelper.replace(context, new ShiftFragment(), null, true);
            } else if (generalMenu.getTitle().equals(context.getString(R.string.menu_schedule))) {
                FragmentHelper.replace(context, new ScheduleFragment(), null, true);
            } else if (generalMenu.getTitle().equals(context.getString(R.string.menu_attendance_report))) {
                FragmentHelper.replace(context, new AttendanceReportFragment(), null, true);
            } else if (generalMenu.getTitle().equals("Notification")) {
                FragmentHelper.replace(context, new NotificationFragment(), null, true);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class AttendanceMenuViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.image)
        ImageView image;
        private View itemView;

        AttendanceMenuViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            ButterKnife.bind(this, itemView);
        }
    }
}
