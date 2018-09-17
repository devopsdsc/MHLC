package red.point.checkpoint.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import red.point.checkpoint.R;
import red.point.checkpoint.api.model.Pin;
import red.point.checkpoint.util.DateUtil;

public class PinDailyReportAdapter extends RecyclerView.Adapter<PinDailyReportAdapter.ReportViewHolder> {

    private static final String TAG = PinDailyReportAdapter.class.getSimpleName();
    private List<Pin> list;
    private Bundle savedInstanceState;
    private Context context;

    public PinDailyReportAdapter(List<Pin> list) {
        this.list = list;
    }

    public PinDailyReportAdapter(List<Pin> list, Bundle savedInstanceState, Context context) {
        this.list = list;
        this.savedInstanceState = savedInstanceState;
        this.context = context;
    }

    @Override
    public ReportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ReportViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_pin_daily_report_list, parent, false));
    }

    @Override
    public void onBindViewHolder(final ReportViewHolder holder, int position) {
        final Pin pin = list.get(position);

        if (pin.getPinLocation() != null) {
            holder.tag.setVisibility(View.VISIBLE);
            holder.tag.setText(pin.getPinLocation().getName());
        }

        holder.date.setText(DateUtil.timestampToDateTime(pin.getCreatedAt().getTimezone(), pin.getCreatedAt().getDate()));
        holder.address.setText(pin.getAddress());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView tag, date, address;

        ReportViewHolder(View itemView) {
            super(itemView);

            tag = itemView.findViewById(R.id.tag);
            date = itemView.findViewById(R.id.date);
            address = itemView.findViewById(R.id.address);
        }
    }
}
