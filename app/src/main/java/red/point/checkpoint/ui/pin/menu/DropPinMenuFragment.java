package red.point.checkpoint.ui.pin.menu;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import red.point.checkpoint.R;
import red.point.checkpoint.di.ContextModule;
import red.point.checkpoint.helper.PrefManager;
import red.point.checkpoint.ui.MainFragment;
import red.point.checkpoint.util.ProgressDialogUtil;
import red.point.checkpoint.util.RecyclerViewUtil;

/**
 * A simple {@link Fragment} subclass.
 */
public class DropPinMenuFragment extends MainFragment {

    @Inject
    PrefManager prefManager;

    @BindView(R.id.rv_drop_pin_menu)
    RecyclerView rvDropPinMenu;

    private List<DropPinMenu> listDropPinMenu = new ArrayList<>();

    private Unbinder unbinder;

    public DropPinMenuFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_drop_pin_menu, container, false);

        unbinder = ButterKnife.bind(this, rootView);

        DropPinMenuComponent dropPinMenuComponent = DaggerDropPinMenuComponent.builder()
                .contextModule(new ContextModule(getContext()))
                .build();

        dropPinMenuComponent.inject(this);

        generateDropPinMenu();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getActivity() != null) getActivity().setTitle("Sales Visitation");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (unbinder != null) unbinder.unbind();
        ProgressDialogUtil.dismiss();
    }

    private void generateDropPinMenu() {
        String[] title = {getString(R.string.menu_company), getString(R.string.menu_wallet), getString(R.string.menu_pin_location), getString(R.string.menu_pin_report)};
        int[] image = {R.drawable.ic_company_fill, R.drawable.ic_wallet_fill, R.drawable.ic_pin_location_fill, R.drawable.ic_notepad_fill};
        int[] color = {R.color.home_icon, R.color.wallet_icon, R.color.setting_icon, R.color.report_icon};
        listDropPinMenu.clear();
        for (int i = 0; i < title.length; i++) {
            DropPinMenu dropPinMenu = new DropPinMenu(title[i], image[i], color[i]);
            listDropPinMenu.add(dropPinMenu);
        }

        DropPinMenuAdapter dropPinMenuAdapter = new DropPinMenuAdapter(getActivity(), listDropPinMenu);
        RecyclerViewUtil.setGridLayout(getContext(), rvDropPinMenu, 4);
        rvDropPinMenu.setAdapter(dropPinMenuAdapter);
        dropPinMenuAdapter.notifyDataSetChanged();
    }
}
