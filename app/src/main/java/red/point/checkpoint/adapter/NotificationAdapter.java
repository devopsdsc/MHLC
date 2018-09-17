package red.point.checkpoint.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import red.point.checkpoint.R;
import red.point.checkpoint.api.model.Notification;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private static final String TAG = "Notification Adapter";
    private List<Notification> list;

    public NotificationAdapter(List<Notification> list) {
        this.list = list;
    }

    @Override
    public NotificationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new NotificationViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_notification_list, parent, false));
    }

    @Override
    public void onBindViewHolder(final NotificationViewHolder holder, int position) {
        Notification notification = list.get(position);
        holder.date.setText(notification.getDate());
        holder.message.setText(notification.getMessage());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView date;
        TextView message;

        NotificationViewHolder(View itemView) {
            super(itemView);

            date = itemView.findViewById(R.id.date);
            message = itemView.findViewById(R.id.message);
        }
    }
}
