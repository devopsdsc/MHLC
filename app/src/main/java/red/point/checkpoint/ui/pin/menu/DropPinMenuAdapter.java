package red.point.checkpoint.ui.pin.menu;

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
import red.point.checkpoint.ui.company.CompanyFragment;
import red.point.checkpoint.ui.pin.location.PinLocationFragment;
import red.point.checkpoint.ui.pin.report.PinReportFragment;
import red.point.checkpoint.ui.wallet.WalletFragment;

public class DropPinMenuAdapter extends RecyclerView.Adapter<DropPinMenuAdapter.DropPinMenuViewHolder> {

    private static final String TAG = DropPinMenuAdapter.class.getSimpleName();
    private FragmentActivity context;
    private List<DropPinMenu> list;

    DropPinMenuAdapter(FragmentActivity context, List<DropPinMenu> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public DropPinMenuViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DropPinMenuViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_drop_pin_menu_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DropPinMenuViewHolder holder, int position) {
        DropPinMenu dropPinMenu = list.get(position);
        holder.image.setImageResource(dropPinMenu.getImage());
        holder.image.setColorFilter(ContextCompat.getColor(context, dropPinMenu.getColor()));
        holder.title.setText(dropPinMenu.getTitle());

        holder.itemView.setOnClickListener(v -> {
            if (dropPinMenu.getTitle().equals(context.getString(R.string.menu_company))) {
                FragmentHelper.replace(context, new CompanyFragment(), null, true);
            } else if (dropPinMenu.getTitle().equals(context.getString(R.string.menu_wallet))) {
                FragmentHelper.replace(context, new WalletFragment(), null, true);
            } else if (dropPinMenu.getTitle().equals(context.getString(R.string.menu_pin_location))) {
                FragmentHelper.replace(context, new PinLocationFragment(), null, true);
            } else if (dropPinMenu.getTitle().equals(context.getString(R.string.menu_pin_report))) {
                FragmentHelper.replace(context, new PinReportFragment(), null, true);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class DropPinMenuViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.image)
        ImageView image;
        private View itemView;

        DropPinMenuViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            ButterKnife.bind(this, itemView);
        }
    }
}
