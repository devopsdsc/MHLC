package red.point.checkpoint.ui.pin.report;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.shimmer.ShimmerFrameLayout;

import org.parceler.Parcels;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import red.point.checkpoint.R;
import red.point.checkpoint.adapter.PinReportAdapter;
import red.point.checkpoint.api.model.Employee;
import red.point.checkpoint.api.model.EmployeeList;
import red.point.checkpoint.api.model.Pin;
import red.point.checkpoint.api.model.PinList;
import red.point.checkpoint.api.service.EmployeeService;
import red.point.checkpoint.api.service.PinService;
import red.point.checkpoint.di.ContextModule;
import red.point.checkpoint.dialog.MonthYearPickerDialogFragment;
import red.point.checkpoint.helper.FragmentHelper;
import red.point.checkpoint.helper.PrefManager;
import red.point.checkpoint.helper.RecyclerItemClickListener;
import red.point.checkpoint.util.DateUtil;
import red.point.checkpoint.util.ProgressDialogUtil;
import red.point.checkpoint.util.RecyclerViewUtil;
import red.point.checkpoint.util.ToastUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class PinReportFragment extends Fragment {

    private static final String TAG = PinReportFragment.class.getSimpleName();

    private Unbinder unbinder;

    @BindView(R.id.shimmer_view_container) ShimmerFrameLayout shimmerFrameLayout;
    @BindView(R.id.list_item) RecyclerView rvReport;
    @BindView(R.id.date) TextView mDate;
    @BindView(R.id.employee) TextView mEmployee;
    @BindView(R.id.emptyLayout) RelativeLayout emptyLayout;
    @BindView(R.id.emptyIcon) ImageView emptyIcon;
    @BindView(R.id.emptyText) TextView emptyText;

    @Inject PrefManager prefManager;
    @Inject EmployeeService employeeService;
    @Inject PinService pinService;

    private Call<EmployeeList> employeeListCall;
    private Call<PinList> pinListCall;

    private PinReportAdapter pinReportAdapter;
    private List<Pin> pinsResult = new ArrayList<>();
    private String reportContent;
    private long employeeId;
    private String employeeName;
    private String startDate = DateUtil.getCurrentDbDate();
    private String endDate = DateUtil.getCurrentDbDate();
    private boolean isDataLoaded = false;
    private boolean isExportClicked = false;

    public PinReportFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_pin_report, container, false);

        unbinder = ButterKnife.bind(this, rootView);

        PinReportComponent pinReportComponent = DaggerPinReportComponent.builder()
                .contextModule(new ContextModule(getContext()))
                .pinReportModule(new PinReportModule())
                .build();

        pinReportComponent.injectPinReport(this);

        getActivity().setTitle(getString(R.string.drop_pin_report));

        shimmerFrameLayout.startShimmerAnimation();

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        startDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, 1);
        endDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        mDate.setText(String.format(Locale.getDefault(), "%02d/%02d", month, year));

        RecyclerViewUtil.setDefault(getContext(), rvReport);
        pinReportAdapter = new PinReportAdapter(pinsResult);
        rvReport.setAdapter(pinReportAdapter);
        rvReport.addOnItemTouchListener(rvClickListener);

        employeeId = prefManager.getUserId();
        employeeName = prefManager.getUserName();
        mEmployee.setText(prefManager.getUserName());

        emptyText.setText("There is no visitation this month");

        loadData();

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu,inflater);
        inflater.inflate(R.menu.export, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.export :
            {
                isExportClicked = true;
                ProgressDialogUtil.showLoading(getContext());
                if (isDataLoaded) {
                    if (pinsResult.size() == 0) {
                        ToastUtil.show("No dropped pin this month");
                        ProgressDialogUtil.dismiss();
                    } else {
                        sendReport();
                    }
                }
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (unbinder != null) unbinder.unbind();
        if (employeeListCall != null && employeeListCall.isExecuted()) employeeListCall.cancel();
        if (pinListCall != null && pinListCall.isExecuted()) pinListCall.cancel();
        ProgressDialogUtil.dismiss();
    }

    @OnClick(R.id.cv_date)
    public void changeDate() {
        MonthYearPickerDialogFragment pd = new MonthYearPickerDialogFragment();

        pd.setListener((view, year, month, dayOfMonth) -> {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month - 1);

            startDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, 1);
            endDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, cal.getActualMaximum(Calendar.DAY_OF_MONTH));

            mDate.setText(String.format(Locale.getDefault(), "%02d/%04d", month, year));

            isExportClicked = false;
            isDataLoaded = false;

            loadData();
        });

        pd.show(getFragmentManager(), "MonthYearPickerDialog");
    }

    @OnClick(R.id.cv_employee)
    public void changeEmployee() {
        ProgressDialogUtil.showLoading(getContext());
        final AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());
        builderSingle.setIcon(R.drawable.ic_employees);
        builderSingle.setTitle("Select employee");

        final ArrayAdapter<String> employeeAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.select_dialog_singlechoice);
        employeeListCall = employeeService.getEmployees(prefManager.getCompanyId());
        employeeListCall.enqueue(new Callback<EmployeeList>() {
            @Override
            public void onResponse(Call<EmployeeList> call, Response<EmployeeList> response) {
                ProgressDialogUtil.dismiss();
                if (response.isSuccessful()) {
                    List<Employee> employees = response.body().getEmployees();
                    for (int i = 0; i < employees.size(); i++) {
                        employeeAdapter.add(employees.get(i).getName());
                        employeeAdapter.notifyDataSetChanged();
                    }

                    builderSingle.setNegativeButton("cancel", (dialog, which) -> dialog.dismiss());
                    builderSingle.setAdapter(employeeAdapter, (dialog, which) -> {
                        employeeId = employees.get(which).getId();
                        employeeName = employees.get(which).getName();
                        mEmployee.setText(employees.get(which).getName());

                        isExportClicked = false;
                        isDataLoaded = false;

                        loadData();
                    });
                    builderSingle.show();
                }

                shimmerFrameLayout.stopShimmerAnimation();
                shimmerFrameLayout.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<EmployeeList> call, Throwable t) {
                ProgressDialogUtil.dismiss();
                if (! call.isCanceled()) {
                    ToastUtil.show("Connection Failure");
                    shimmerFrameLayout.stopShimmerAnimation();
                    shimmerFrameLayout.setVisibility(View.GONE);
                }
            }
        });
    }

    private void loadData() {
        pinsResult.clear();
        pinReportAdapter.notifyDataSetChanged();

        reportContent = "";

        shimmerFrameLayout.setVisibility(View.VISIBLE);
        shimmerFrameLayout.startShimmerAnimation();

        pinListCall = pinService.getPins(prefManager.getCompanyId(), employeeId, startDate, endDate);
        pinListCall.enqueue(new Callback<PinList>() {
            @Override
            public void onResponse(Call<PinList> call, Response<PinList> response) {
                if (response.isSuccessful()) {
                    List<Pin> pins = response.body().getPins();
                    for (int i = 0; i < pins.size(); i++) {
                        Pin pin = pins.get(i);
                        pinsResult.add(pin);
                        pinReportAdapter.notifyDataSetChanged();

                        reportContent = reportContent.concat(DateUtil.timestampToDateTime(pin.getCreatedAt().getTimezone(), pin.getCreatedAt().getDate())).concat(",");

                        if (pin.getPinLocation() != null) {
                            reportContent = reportContent.concat(pin.getPinLocation().getName()).concat(",");
                        } else {
                            reportContent = reportContent.concat(",");
                        }

                        reportContent.concat(pin.getAddress()).concat(",");
                        reportContent = reportContent.concat("\r\n");

                        isDataLoaded = true;

                        if (isExportClicked) {
                            ProgressDialogUtil.showLoading(getContext());
                            isExportClicked = false;
                            if (pinsResult.size() == 0) {
                                ToastUtil.show("No dropped pin this month");
                                ProgressDialogUtil.dismiss();
                            } else {
                                sendReport();
                            }
                        }
                    }
                }

                isDataLoaded = true;

                shimmerFrameLayout.stopShimmerAnimation();
                shimmerFrameLayout.setVisibility(View.GONE);

                ProgressDialogUtil.dismiss();

                loadView();
            }

            @Override
            public void onFailure(Call<PinList> call, Throwable t) {
                if (! call.isCanceled()) {
                    shimmerFrameLayout.stopShimmerAnimation();
                    shimmerFrameLayout.setVisibility(View.GONE);
                }
            }
        });
    }

    private void loadView() {
        if (pinsResult.size() == 0) {
            emptyLayout.setVisibility(View.VISIBLE);
        } else {
            emptyLayout.setVisibility(View.GONE);
        }
    }

    private void sendReport() {
        String reportFileName = "PIN REPORT " + startDate + " - " + endDate + " " + employeeName;

        try {
            String reportHeader = "DATE, TAG, LOCATION";
            File mFile = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "/" + reportFileName + ".csv");
            boolean mFileCreated = mFile.createNewFile();
            FileWriter mFileWriter = new FileWriter(mFile.getAbsoluteFile());
            BufferedWriter mBufferedWriter = new BufferedWriter(mFileWriter);
            mBufferedWriter.write(reportHeader);
            mBufferedWriter.newLine();
            mBufferedWriter.write(reportContent);
            mBufferedWriter.close();

            ProgressDialogUtil.dismiss();

            Uri mUri = FileProvider.getUriForFile(getContext(),
                    getContext().getApplicationContext().getPackageName() +
                            ".red.point.checkpoint.provider", mFile);

            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, "" + reportFileName);
            sendIntent.putExtra(Intent.EXTRA_STREAM, mUri);
            sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            sendIntent.setType("text/html");
            startActivity(Intent.createChooser(sendIntent, "SEND REPORT"));
        } catch (IOException e) {
            ProgressDialogUtil.dismiss();
            e.printStackTrace();
        }
    }

    private RecyclerItemClickListener rvClickListener = new RecyclerItemClickListener(getContext(), rvReport, new RecyclerItemClickListener.OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            Bundle bundle = new Bundle();

            List<Pin> selectedPins = new ArrayList<>();
            Pin pin = pinsResult.get(position);

            for (int i=0; i<pinsResult.size();i++) {
                if (pinsResult.get(i).getCreatedAt().getDate().substring(0, 10).equals(pin.getCreatedAt().getDate().substring(0, 10))) {
                    selectedPins.add(pinsResult.get(i));
                }
            }

            bundle.putParcelable("pins", Parcels.wrap(selectedPins));
            bundle.putString("date", mDate.getText().toString());
            bundle.putString("employeeName", employeeName);

            FragmentHelper.replace(getActivity(), new PinDailyReportFragment(), bundle, true);
        }

        @Override
        public void onLongItemClick(View view, int position) {

        }
    });
}
