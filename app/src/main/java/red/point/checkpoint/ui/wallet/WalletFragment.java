package red.point.checkpoint.ui.wallet;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.SimpleShowcaseEventListener;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import red.point.checkpoint.CustomViewTarget;
import red.point.checkpoint.MyApplication;
import red.point.checkpoint.R;
import red.point.checkpoint.api.model.TopUpResult;
import red.point.checkpoint.api.model.Wallet;
import red.point.checkpoint.api.model.WalletResponse;
import red.point.checkpoint.api.service.CompanyService;
import red.point.checkpoint.api.service.TopUpService;
import red.point.checkpoint.di.ContextModule;
import red.point.checkpoint.helper.FragmentHelper;
import red.point.checkpoint.helper.PrefManager;
import red.point.checkpoint.ui.MainFragment;
import red.point.checkpoint.util.NumberUtil;
import red.point.checkpoint.util.ProgressDialogUtil;
import red.point.checkpoint.util.ToastUtil;
import red.point.checkpoint.util.iap.IabHelper;
import red.point.checkpoint.util.iap.IabResult;
import red.point.checkpoint.util.iap.Inventory;
import red.point.checkpoint.util.iap.Purchase;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WalletFragment extends MainFragment {

    private static final String TAG = WalletFragment.class.getSimpleName();

    // (arbitrary) request code for the purchase flow
    private static final int RC_REQUEST = 10001;

    @Inject PrefManager prefManager;
    @Inject CompanyService companyService;
    @Inject TopUpService topUpService;

    @BindView(R.id.shimmer_view_container) ShimmerFrameLayout shimmerFrameLayout;
    @BindView(R.id.wallet_value) TextView mWalletValue;
    @BindView(R.id.content_view) ScrollView contentView;
    @BindView(R.id.us1) RelativeLayout btnTopUp;
    @BindView(R.id.us2) RelativeLayout btnTopUp2;
    @BindView(R.id.us3) RelativeLayout btnTopUp3;
    @BindView(R.id.us4) RelativeLayout btnTopUp4;
    @BindView(R.id.us5) RelativeLayout btnTopUp5;
    @BindView(R.id.us6) RelativeLayout btnTopUp6;
    @BindView(R.id.us7) RelativeLayout btnTopUp7;

    // Managed in app item
    private String[] managedItemId = {
            "us.1", "us.2", "us.3", "us.4", "us.5", "us.6", "us.7"
    };
    private Double[] managedItemValue = {
            10000.00, 20000.00, 50000.00, 100000.00, 200000.00, 500000.00, 1000000.00
    };

    private String developerPayload;
    private IabHelper mHelper;
    private Boolean isPurchaseFlowRunning = false;

    private Unbinder unbinder;
    private Call<WalletResponse> walletResponseCall;
    private Call<TopUpResult> topUpResultCall;
    private ShowcaseView sv;

    public WalletFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (getActivity() != null)  getActivity().setTitle(R.string.nav_wallet);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_wallet, container, false);

        unbinder = ButterKnife.bind(this, view);

        WalletComponent walletComponent = DaggerWalletComponent.builder()
                .contextModule(new ContextModule(getContext()))
                .walletModule(new WalletModule())
                .build();

        walletComponent.inject(this);

        // Add a shimmering effect to any view in your app.
        // It's useful as an unobtrusive loading indicator.
        shimmerFrameLayout.startShimmerAnimation();

        developerPayload = prefManager.getFirebaseUserId() + prefManager.getFirebaseCompanyId();

        setupWalletValue();

        String base64EncodedPublicKey = getString(R.string.base64PublicKey);

        Log.d(TAG, "Creating IAB helper.");
        mHelper = new IabHelper(getContext(), reverseStringBuffer(base64EncodedPublicKey));

        // enable debug logging (for a production application, you should set this to false).
        mHelper.enableDebugLogging(false, TAG);

        // Start setup. This is asynchronous and the specified listener
        // will be called once setup completes.
        Log.d(TAG, "Starting setup.");
        mHelper.startSetup(result -> {
            Log.d(TAG, "Setup finished.");

            if (!result.isSuccess()) {
                // Oh noes, there was a problem.
                Log.e(TAG, "Problem setting up in-app billing: " + result);
                return;
            }

            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) return;

            // IAB is fully set up. Now, let's get an inventory of stuff we own.
            Log.d(TAG, "Setup successful. Querying inventory.");

            try {
                mHelper.queryInventoryAsync(mGotInventoryListener);
            } catch (IabHelper.IabAsyncInProgressException e) {
                e.printStackTrace();
            }
        });

        btnTopUp.setOnClickListener(new TopUpClickListener());
        btnTopUp2.setOnClickListener(new TopUpClickListener());
        btnTopUp3.setOnClickListener(new TopUpClickListener());
        btnTopUp4.setOnClickListener(new TopUpClickListener());
        btnTopUp5.setOnClickListener(new TopUpClickListener());
        btnTopUp6.setOnClickListener(new TopUpClickListener());
        btnTopUp7.setOnClickListener(new TopUpClickListener());

        return view;
    }

    @OnClick(R.id.voucher)
    public void addVoucher() {
        FragmentHelper.show(getActivity(), new AddVoucherFragment(), null);
    }

    @OnClick(R.id.btn_transaction)
    public void clickBtnTransaction() {
        FragmentHelper.replace(getActivity(), new TransactionFragment(), null, true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (mHelper == null) return;

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        }
        else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
        }
    }

    // Listener that's called when we finish querying the items and subscriptions we own
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.d(TAG, "Query inventory finished.");

            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) return;

            // Is it a failure?
            if (result.isFailure()) {
                Log.e(TAG, "Failed to query inventory: " + result);
                return;
            }

            Log.d(TAG, "Query inventory was successful.");

            /*
             * Check for items we own.
             * Try to consume every item that not already consumed
             */

            for (String aManagedItemId : managedItemId) {
                consume(inventory.getPurchase(aManagedItemId));
            }

            Log.d(TAG, "Initial inventory query finished.");
        }
    };

    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);

            // Purchase flow finished and user can buy another item
            isPurchaseFlowRunning = false;

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
                Log.e(TAG, "Error purchasing: " + result);
                return;
            }

            consume(purchase);
        }
    };

    // Called when consumption is complete
    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(final Purchase purchase, IabResult result) {
            Log.d(TAG, "Consumption finished. Purchase: " + purchase + ", result: " + result);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            // We know this is the "gas" sku because it's the only one we consume,
            // so we don't check which sku was consumed. If you have more than one
            // sku, you probably should check...
            if (result.isSuccess()) {
                // successfully consumed, so we apply the effects of the item in our
                // game world's logic, which in our case means filling the gas tank a bit
                Log.d(TAG, "Consumption successful. Provisioning.");

                double value = 0;
                for (int i=0;i<managedItemId.length;i++) {
                    if (managedItemId[i].equals(purchase.getSku())) {
                        value = managedItemValue[i];
                    }
                }

                topUpResultCall = topUpService.topUp(
                        prefManager.getCompanyId(),
                        prefManager.getUserId(),
                        purchase.getOrderId(),
                        purchase.getItemType(),
                        purchase.getSignature(),
                        purchase.getPurchaseState(),
                        purchase.getPurchaseTime(),
                        purchase.getDeveloperPayload(),
                        purchase.getSku(),
                        purchase.getToken(),
                        value);

                topUpResultCall.enqueue(new Callback<TopUpResult>() {
                    @Override
                    public void onResponse(Call<TopUpResult> call, Response<TopUpResult> response) {
                        if (response.isSuccessful()) {
                            ToastUtil.show("Top Up Success");
                        }
                    }

                    @Override
                    public void onFailure(Call<TopUpResult> call, Throwable t) {
                        if (! call.isCanceled()) {
                            ToastUtil.show(t.getLocalizedMessage());
                        }
                    }
                });
            } else {
                Log.d(TAG, "Error while consuming: " + result);
            }
            Log.d(TAG, "End consumption flow.");
        }
    };

    /** Verifies the developer payload of a purchase. */
    private boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();

        /*
         * Verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */
        if (payload.equals(developerPayload)) {
            Log.d(TAG, "Payload verified.");
            return true;
        } else {
            Log.d(TAG, "Payload not verified.");
            return false;
        }
    }

    // Reverse using StringBuffer
    private static String reverseStringBuffer(String s) {
        return new StringBuffer(s).reverse().toString();
    }

    private void onTopupWalletClicked(View arg0, String sku) {
        // launch purchase flow
        // We will be notified of completion via mPurchaseFinishedListener
        if (! isPurchaseFlowRunning) {
            isPurchaseFlowRunning = true;
            try {
                mHelper.launchPurchaseFlow(MyApplication.getInstance().getActivity(), sku, RC_REQUEST, mPurchaseFinishedListener, developerPayload);
            } catch (IabHelper.IabAsyncInProgressException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Check for items we own. Notice that for each purchase, we check
     * the developer payload to see if it's correct! See
     * verifyDeveloperPayload().
     */
    private void consume(Purchase purchase) {
        if (purchase == null) {
            Log.e(TAG, "No purchasing found.");
            return;
        }

        if (!verifyDeveloperPayload(purchase)) {
            Log.e(TAG, "Authenticity verification failed.");
            return;
        }

        try {
            mHelper.consumeAsync(purchase, mConsumeFinishedListener);
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
    }

    private void setupWalletValue() {
        walletResponseCall = companyService.getWallet(prefManager.getCompanyId());
        walletResponseCall.enqueue(new retrofit2.Callback<WalletResponse>() {
            @Override
            public void onResponse(Call<WalletResponse> call, Response<WalletResponse> response) {
                if (response.isSuccessful()) {
                    Wallet wallet = response.body().getWallet();
                    mWalletValue.setText(String.format("%s", NumberUtil.getFormattedNumber(wallet.getValue())));

                    if (prefManager.isFirstTimeSeeWallet()) {
                        prefManager.setFirstTimeSeeWallet(false);

                        Button endButton = new Button(getContext());
                        endButton.setText("");
                        endButton.setEnabled(false);
                        endButton.setVisibility(View.GONE);
                        sv = new ShowcaseView.Builder(getActivity())
                                .withMaterialShowcase()
                                .setStyle(R.style.CustomShowcaseTheme)
                                .setTarget(new CustomViewTarget(mWalletValue, "left"))
                                .replaceEndButton(endButton)
                                .hideOnTouchOutside()
                                .setContentTitle("MY WALLET\n")
                                .setContentText("Shows you the current value of your wallet.\n\n" +
                                        "Your membership will be charged every month and your transaction history can be accessed here.")
                                .setShowcaseEventListener(new SimpleShowcaseEventListener() {

                                    @Override
                                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {

                                    }
                                })
                                .build();
                    }
                }

                shimmerFrameLayout.setVisibility(View.GONE);
                shimmerFrameLayout.stopShimmerAnimation();

                contentView.setVisibility(View.VISIBLE);
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

    private class TopUpClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.us1:
                    onTopupWalletClicked(v, managedItemId[0]);
                    break;
                case R.id.us2:
                    onTopupWalletClicked(v, managedItemId[1]);
                    break;
                case R.id.us3:
                    onTopupWalletClicked(v, managedItemId[2]);
                    break;
                case R.id.us4:
                    onTopupWalletClicked(v, managedItemId[3]);
                    break;
                case R.id.us5:
                    onTopupWalletClicked(v, managedItemId[4]);
                    break;
                case R.id.us6:
                    onTopupWalletClicked(v, managedItemId[5]);
                    break;
                case R.id.us7:
                    onTopupWalletClicked(v, managedItemId[6]);
                    break;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();

        if (sv != null && sv.isShowing()) sv.hide();

        if (walletResponseCall != null && walletResponseCall.isExecuted()) walletResponseCall.cancel();
        if (topUpResultCall != null && topUpResultCall.isExecuted()) topUpResultCall.cancel();

        ProgressDialogUtil.dismiss();
    }
}
