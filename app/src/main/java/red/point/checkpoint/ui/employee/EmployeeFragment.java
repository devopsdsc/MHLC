package red.point.checkpoint.ui.employee;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;

import org.parceler.Parcels;

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
import red.point.checkpoint.api.model.Wallet;
import red.point.checkpoint.api.model.WalletResponse;
import red.point.checkpoint.api.service.CompanyService;
import red.point.checkpoint.api.service.EmployeeService;
import red.point.checkpoint.di.ContextModule;
import red.point.checkpoint.helper.FragmentHelper;
import red.point.checkpoint.helper.PrefManager;
import red.point.checkpoint.helper.RecyclerItemClickListener;
import red.point.checkpoint.ui.MainFragment;
import red.point.checkpoint.util.ProgressDialogUtil;
import red.point.checkpoint.util.RecyclerViewUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployeeFragment extends MainFragment {

    private static final String TAG = EmployeeFragment.class.getSimpleName();

    @Inject PrefManager prefManager;
    @Inject EmployeeService employeeService;
    @Inject CompanyService companyService;

    @BindView(R.id.shimmer_view_container) ShimmerFrameLayout shimmerFrameLayout;
    @BindView(R.id.list_item) RecyclerView recyclerView;

    private Unbinder unbinder;
    private Call<EmployeeList> employeeListCall;
    private Call<WalletResponse> walletResponseCall;
    private EmployeeAdapter adapter;
    private List<Employee> result = new ArrayList<>();
    private List<Employee> employee = new ArrayList<>();

    public EmployeeFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (getActivity() != null) getActivity().setTitle(R.string.nav_employee);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_employee, container, false);

        unbinder = ButterKnife.bind(this, rootView);

        EmployeeComponent employeeComponent = DaggerEmployeeComponent.builder()
                .contextModule(new ContextModule(getContext()))
                .employeeModule(new EmployeeModule())
                .build();

        employeeComponent.inject(this);

        // Add a shimmering effect to any view in your app.
        // It's useful as an unobtrusive loading indicator.
        shimmerFrameLayout.startShimmerAnimation();

        RecyclerViewUtil.setDefault(getContext(), recyclerView);
        adapter = new EmployeeAdapter(result);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnItemTouchListener(EmployeeTouchListener);

        loadData();

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();

        if (employeeListCall != null && employeeListCall.isExecuted()) employeeListCall.cancel();
        if (walletResponseCall != null && walletResponseCall.isExecuted()) walletResponseCall.cancel();

        ProgressDialogUtil.dismiss();
    }

    @OnClick(R.id.fab_add)
    public void setBtnAddOnClickListener() {
        ProgressDialogUtil.showLoading(getContext());
        walletResponseCall = companyService.getWallet(prefManager.getCompanyId());
        walletResponseCall.enqueue(new retrofit2.Callback<WalletResponse>() {
            @Override
            public void onResponse(Call<WalletResponse> call, Response<WalletResponse> response) {
                if (response.body() != null) {
                    Wallet wallet = response.body().getWallet();
                    boolean isWalletEnough = false;

                    Bundle bundle = new Bundle();
                    if (wallet.getValue() >= 10000) {
                        isWalletEnough = true;
                    }
                    bundle.putBoolean("isWalletEnough", isWalletEnough);

                    FragmentHelper.show(getActivity(), new InviteEmployeeFragment(), bundle);
                }

                ProgressDialogUtil.dismiss();
            }

            @Override
            public void onFailure(Call<WalletResponse> call, Throwable t) {
                if (! call.isCanceled()) {
                    Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                    ProgressDialogUtil.dismiss();
                }
            }
        });
    }

    private RecyclerItemClickListener EmployeeTouchListener = new RecyclerItemClickListener(getContext(), recyclerView ,new RecyclerItemClickListener.OnItemClickListener() {
        @Override public void onItemClick(View view, int position) {
            Bundle bundle = new Bundle();
            bundle.putParcelable("employee", Parcels.wrap(result.get(position)));

            FragmentHelper.replace(getActivity(), new EmployeeProfileFragment(), bundle, true);
        }

        @Override
        public void onLongItemClick(View view, int position) {

        }
    });

    private void loadData() {
        result.clear();
        adapter.notifyDataSetChanged();

        employeeListCall = employeeService.getEmployees(prefManager.getCompanyId());
        employeeListCall.enqueue(new Callback<EmployeeList>() {
            @Override
            public void onResponse(Call<EmployeeList> call, Response<EmployeeList> response) {
                if (response.isSuccessful()) {
                    employee = response.body().getEmployees();
                    result.addAll(employee);
                    adapter.notifyDataSetChanged();
                }

                shimmerFrameLayout.stopShimmerAnimation();
                shimmerFrameLayout.setVisibility(View.GONE);
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
}
