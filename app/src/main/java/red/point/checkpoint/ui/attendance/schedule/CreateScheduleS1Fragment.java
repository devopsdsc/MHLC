package red.point.checkpoint.ui.attendance.schedule;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import red.point.checkpoint.R;
import red.point.checkpoint.adapter.EmployeeAdapter;
import red.point.checkpoint.api.model.Employee;
import red.point.checkpoint.api.model.EmployeeList;
import red.point.checkpoint.api.service.EmployeeService;
import red.point.checkpoint.di.ContextModule;
import red.point.checkpoint.helper.FragmentHelper;
import red.point.checkpoint.helper.PrefManager;
import red.point.checkpoint.helper.RecyclerItemClickListener;
import red.point.checkpoint.ui.MainFragment;
import red.point.checkpoint.util.ProgressDialogUtil;
import red.point.checkpoint.util.RecyclerViewUtil;
import red.point.checkpoint.util.ToastUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class CreateScheduleS1Fragment extends MainFragment {

    private static final String TAG = CreateScheduleS1Fragment.class.getSimpleName();

    @Inject PrefManager prefManager;
    @Inject EmployeeService employeeService;

    @BindView(R.id.list_item) RecyclerView recyclerView;
    @BindView(R.id.shimmer_view_container) ShimmerFrameLayout shimmerFrameLayout;

    private Unbinder unbinder;
    private Call<EmployeeList> employeeListCall;
    private EmployeeAdapter adapter;
    private List<Employee> result = new ArrayList<>();

    public CreateScheduleS1Fragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (getActivity() != null) getActivity().setTitle("Add Schedule");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_create_schedule_s1, container, false);

        unbinder = ButterKnife.bind(this, view);

        CreateScheduleS1Component createScheduleS1Component = DaggerCreateScheduleS1Component.builder()
                .createScheduleS1Module(new CreateScheduleS1Module())
                .contextModule(new ContextModule(getContext()))
                .build();

        createScheduleS1Component.inject(this);

        // Add a shimmering effect to any view in your app.
        // It's useful as an unobtrusive loading indicator.
        shimmerFrameLayout.startShimmerAnimation();

        RecyclerViewUtil.setDefault(getContext(), recyclerView);
        adapter = new EmployeeAdapter(result);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnItemTouchListener(EmployeeTouchListener);

        loadData();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();

        if (employeeListCall != null && employeeListCall.isExecuted()) employeeListCall.cancel();

        ProgressDialogUtil.dismiss();
    }

    private void loadData() {
        employeeListCall = employeeService.getEmployees(prefManager.getCompanyId());
        employeeListCall.enqueue(new Callback<EmployeeList>() {
            @Override
            public void onResponse(Call<EmployeeList> call, Response<EmployeeList> response) {
                shimmerFrameLayout.stopShimmerAnimation();
                shimmerFrameLayout.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    List<Employee> employee = response.body().getEmployees();
                    result.clear();
                    result.addAll(employee);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<EmployeeList> call, Throwable t) {
                if (! call.isCanceled()) {
                    Toast.makeText(getContext(), "Connection Failure", Toast.LENGTH_SHORT).show();
                    shimmerFrameLayout.stopShimmerAnimation();
                    shimmerFrameLayout.setVisibility(View.GONE);
                }
            }
        });
    }

    @OnClick(R.id.cv_employee)
    public void clickBtnEmployee() {
        ToastUtil.show("Choose employee");
    }

    @OnClick(R.id.cv_branch)
    public void clickBtnBranch() {
        ToastUtil.show("Choose employee first");
    }

    private RecyclerItemClickListener EmployeeTouchListener = new RecyclerItemClickListener(getContext(), recyclerView ,new RecyclerItemClickListener.OnItemClickListener() {
        @Override public void onItemClick(View view, int position) {
            Bundle bundle = new Bundle();
            bundle.putLong("employeeId", result.get(position).getId());
            bundle.putString("employeeName", result.get(position).getName());

            FragmentHelper.replace(getActivity(), new CreateScheduleS2Fragment(), bundle, true);
        }

        @Override public void onLongItemClick(View view, int position) {
            //
        }
    });
}
