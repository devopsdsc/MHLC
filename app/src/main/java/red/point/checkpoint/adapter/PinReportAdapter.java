package red.point.checkpoint.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import red.point.checkpoint.R;
import red.point.checkpoint.api.model.Pin;
import red.point.checkpoint.util.DateUtil;

public class PinReportAdapter extends RecyclerView.Adapter<PinReportAdapter.ReportViewHolder> {

    private static final String TAG = PinReportAdapter.class.getSimpleName();

    private List<Pin> list;

    public PinReportAdapter(List<Pin> list) {
        this.list = list;
    }

    @Override
    public ReportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ReportViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_pin_report_list, parent, false));
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
