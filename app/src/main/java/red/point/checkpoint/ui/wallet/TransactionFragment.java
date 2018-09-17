package red.point.checkpoint.ui.wallet;


import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import red.point.checkpoint.R;
import red.point.checkpoint.adapter.TransactionAdapter;
import red.point.checkpoint.api.model.Transaction;
import red.point.checkpoint.api.model.TransactionList;
import red.point.checkpoint.api.model.Wallet;
import red.point.checkpoint.api.model.WalletResponse;
import red.point.checkpoint.api.service.CompanyService;
import red.point.checkpoint.api.service.TransactionService;
import red.point.checkpoint.di.ContextModule;
import red.point.checkpoint.helper.FragmentHelper;
import red.point.checkpoint.helper.PrefManager;
import red.point.checkpoint.ui.MainFragment;
import red.point.checkpoint.util.NumberUtil;
import red.point.checkpoint.util.ProgressDialogUtil;
import red.point.checkpoint.util.RecyclerViewUtil;
import red.point.checkpoint.util.ToastUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionFragment extends MainFragment {

    private static final String TAG = TransactionFragment.class.getSimpleName();

    @Inject PrefManager prefManager;
    @Inject TransactionService transactionService;
    @Inject CompanyService companyService;

    @BindView(R.id.list_item) RecyclerView recyclerView;
    @BindView(R.id.shimmer_view_container) ShimmerFrameLayout shimmerFrameLayout;
    @BindView(R.id.wallet_value) TextView mWalletValue;

    private Unbinder unbinder;
    private Call<TransactionList> transactionListCall;
    private Call<WalletResponse> walletResponseCall;
    private TransactionAdapter adapter;
    private List<Transaction> result = new ArrayList<>();


    public TransactionFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (getActivity() != null) getActivity().setTitle(R.string.nav_transaction);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_transaction, container, false);

        unbinder = ButterKnife.bind(this, view);

        TransactionComponent transactionComponent = DaggerTransactionComponent.builder()
                .transactionModule(new TransactionModule())
                .contextModule(new ContextModule(getContext()))
                .build();

        transactionComponent.inject(this);

        // Add a shimmering effect to any view in your app.
        // It's useful as an unobtrusive loading indicator.
        shimmerFrameLayout.startShimmerAnimation();

        setupWalletValue();

        RecyclerViewUtil.setDefault(getContext(), recyclerView);
        adapter = new TransactionAdapter(result);
        recyclerView.setAdapter(adapter);

        loadData();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();

        if (walletResponseCall != null && walletResponseCall.isExecuted()) walletResponseCall.cancel();
        if (transactionListCall != null && transactionListCall.isExecuted()) transactionListCall.cancel();

        ProgressDialogUtil.dismiss();
    }

    @OnClick(R.id.btn_topup)
    public void btnTopUpClick() {
        FragmentHelper.popBackStack(getActivity(), WalletFragment.class.getSimpleName());
    }

    private void loadData() {
        result.clear();
        adapter.notifyDataSetChanged();
        
        transactionListCall = transactionService.getTransactions(prefManager.getCompanyId());
        transactionListCall.enqueue(new Callback<TransactionList>() {
            @Override
            public void onResponse(Call<TransactionList> call, Response<TransactionList> response) {
                if (response.isSuccessful()) {
                    List<Transaction> transactions = Objects.requireNonNull(response.body()).getTransactions();
                    result.addAll(transactions);
                    adapter.notifyDataSetChanged();
                }

                shimmerFrameLayout.setVisibility(View.GONE);
                shimmerFrameLayout.stopShimmerAnimation();
            }

            @Override
            public void onFailure(Call<TransactionList> call, Throwable t) {
                if (! call.isCanceled()) {
                    Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                    shimmerFrameLayout.stopShimmerAnimation();
                    shimmerFrameLayout.setVisibility(View.GONE);
                }
            }
        });
    }

    private void setupWalletValue() {
        walletResponseCall = companyService.getWallet(prefManager.getCompanyId());
        walletResponseCall.enqueue(new retrofit2.Callback<WalletResponse>() {
            @Override
            public void onResponse(Call<WalletResponse> call, Response<WalletResponse> response) {
                if (response.isSuccessful()) {
                    Wallet wallet = response.body().getWallet();
                    mWalletValue.setText(String.format("%s", NumberUtil.getFormattedNumber(wallet.getValue())));
                }

                shimmerFrameLayout.setVisibility(View.GONE);
                shimmerFrameLayout.stopShimmerAnimation();
            }

            @Override
            public void onFailure(Call<WalletResponse> call, Throwable t) {
                if (! call.isCanceled()) {
                    ToastUtil.showConnectionFailure();
                    shimmerFrameLayout.setVisibility(View.GONE);
                    shimmerFrameLayout.stopShimmerAnimation();
                }
            }
        });
    }
}
