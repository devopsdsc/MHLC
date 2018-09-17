package red.point.checkpoint.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import red.point.checkpoint.R;
import red.point.checkpoint.api.model.ReferralSource;

public class ReferralSourceAdapter extends RecyclerView.Adapter<ReferralSourceAdapter.ReferralSourceViewHolder> {

    private List<ReferralSource> list;

    public ReferralSourceAdapter(List<ReferralSource> list) {
        this.list = list;
    }

    @Override
    public ReferralSourceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ReferralSourceViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_referral_source_list, parent, false));
    }

    @Override
    public void onBindViewHolder(ReferralSourceViewHolder holder, int position) {
        ReferralSource referralSource = list.get(position);
        holder.name.setText(referralSource.getName());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ReferralSourceViewHolder extends RecyclerView.ViewHolder {
        TextView name;

        ReferralSourceViewHolder(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
        }
    }
}
