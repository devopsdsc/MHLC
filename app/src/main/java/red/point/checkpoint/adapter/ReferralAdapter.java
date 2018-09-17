package red.point.checkpoint.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import red.point.checkpoint.R;
import red.point.checkpoint.api.model.Referral;

public class ReferralAdapter extends RecyclerView.Adapter<ReferralAdapter.ReferenceViewHolder> {

    private List<Referral> list;

    public ReferralAdapter(List<Referral> list) {
        this.list = list;
    }

    @Override
    public ReferenceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ReferenceViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_referral_list, parent, false));
    }

    @Override
    public void onBindViewHolder(ReferenceViewHolder holder, int position) {
        Referral reference = list.get(position);
        holder.name.setText(reference.getName());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ReferenceViewHolder extends RecyclerView.ViewHolder {
        TextView name;

        ReferenceViewHolder(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
        }
    }
}
