package red.point.checkpoint.util;

import android.content.Context;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.LinearLayout;

public class RecyclerViewUtil {

    public static void setDefault(Context context, RecyclerView recyclerView) {
        recyclerView.setHasFixedSize(true);

        addDivider(recyclerView, LinearLayout.VERTICAL);

        setVerticalLayout(context, recyclerView);
    }

    public static void setVerticalLayout(Context context, RecyclerView recyclerView) {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(context);
        mLayoutManager.setOrientation(LinearLayout.VERTICAL);
        recyclerView.setLayoutManager(mLayoutManager);
    }

    public static void setHorizontalLayout(Context context, RecyclerView recyclerView) {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(context);
        mLayoutManager.setOrientation(LinearLayout.HORIZONTAL);
        recyclerView.setLayoutManager(mLayoutManager);
    }

    public static void setGridLayout(Context context, RecyclerView recyclerView, int spanCount) {
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(context, spanCount);
        recyclerView.setLayoutManager(mLayoutManager);
    }

    public static void addDivider(RecyclerView recyclerView, int orientation) {
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), orientation));
    }
}
