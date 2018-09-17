package red.point.checkpoint.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import red.point.checkpoint.R;
import red.point.checkpoint.api.model.Transaction;
import red.point.checkpoint.util.NumberUtil;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> list;

    public TransactionAdapter(List<Transaction> list) {
        this.list = list;
    }

    @Override
    public TransactionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TransactionViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_transaction_list, parent, false));
    }

    @Override
    public void onBindViewHolder(TransactionViewHolder holder, int position) {
        Transaction transaction = list.get(position);
        holder.date.setText(transaction.getDate());
        holder.type.setText(transaction.getType());
        holder.value.setText(String.format("%s", NumberUtil.getFormattedNumber(transaction.getValue())));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView date;
        TextView type;
        TextView value;

        TransactionViewHolder(View itemView) {
            super(itemView);

            date = itemView.findViewById(R.id.date);
            type = itemView.findViewById(R.id.type);
            value = itemView.findViewById(R.id.value);
        }
    }
}
