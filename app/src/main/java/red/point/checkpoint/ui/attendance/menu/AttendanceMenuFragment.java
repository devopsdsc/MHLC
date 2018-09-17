package red.point.checkpoint.ui.attendance.menu;


import android.content.Context;
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
public class AttendanceMenuFragment extends MainFragment {


    @Inject
    PrefManager prefManager;

    @BindView(R.id.rv_attendance_menu)
    RecyclerView rvAttendanceMenu;

    private List<AttendanceMenu> listAttendanceMenu = new ArrayList<>();

    private Unbinder unbinder;

    public AttendanceMenuFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (getActivity() != null) getActivity().setTitle(R.string.attendance);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_attendance_menu, container, false);

        unbinder = ButterKnife.bind(this, rootView);

        AttendanceMenuComponent attendanceMenuCompoment = DaggerAttendanceMenuComponent.builder()
                .contextModule(new ContextModule(getContext()))
                .build();

        attendanceMenuCompoment.inject(this);

        generateAttendanceMenu();

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (unbinder != null) unbinder.unbind();
        ProgressDialogUtil.dismiss();
    }

    private void generateAttendanceMenu() {
        String[] titleGeneral = {
                getString(R.string.menu_company), getString(R.string.menu_wallet), getString(R.string.menu_setting), getString(R.string.menu_attendance_report),
                getString(R.string.menu_branch), getString(R.string.menu_employee), getString(R.string.menu_shift), getString(R.string.menu_schedule),
                "Notification"};
        int[] imageGeneral = {
                R.drawable.ic_company_fill, R.drawable.ic_wallet_fill, R.drawable.ic_controls_fill, R.drawable.ic_file_fill,
                R.drawable.ic_store_fill, R.drawable.ic_employee_fill, R.drawable.ic_clock_fill, R.drawable.ic_calendar_fill,
                R.drawable.ic_alarm};
        int[] colorGeneral = {
                R.color.home_icon, R.color.wallet_icon, R.color.setting_icon, R.color.report_icon,
                R.color.branch_icon, R.color.employee_icon, R.color.shift_icon, R.color.schedule_icon,
                R.color.wallet_icon};
        listAttendanceMenu.clear();
        for (int i=0; i<titleGeneral.length; i++) {
            AttendanceMenu attendanceMenu = new AttendanceMenu(titleGeneral[i],imageGeneral[i],colorGeneral[i]);
            listAttendanceMenu.add(attendanceMenu);
        }

        AttendanceMenuAdapter attendanceMenuAdapter = new AttendanceMenuAdapter(getActivity(), listAttendanceMenu);
        RecyclerViewUtil.setGridLayout(getContext(), rvAttendanceMenu, 4);
        rvAttendanceMenu.setAdapter(attendanceMenuAdapter);
        attendanceMenuAdapter.notifyDataSetChanged();
    }

}
