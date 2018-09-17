package red.point.checkpoint.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.bottomnavigation.LabelVisibilityMode;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomNavigationView.OnNavigationItemSelectedListener;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import red.point.checkpoint.BuildConfig;
import red.point.checkpoint.DaggerMainActivityComponent;
import red.point.checkpoint.MainActivityComponent;
import red.point.checkpoint.MainActivityModule;
import red.point.checkpoint.MyApplication;
import red.point.checkpoint.R;
import red.point.checkpoint.api.model.ReferralChecking;
import red.point.checkpoint.api.model.User;
import red.point.checkpoint.api.model.UserResponse;
import red.point.checkpoint.api.service.ReferralService;
import red.point.checkpoint.api.service.UserService;
import red.point.checkpoint.di.ContextModule;
import red.point.checkpoint.helper.LocaleHelper;
import red.point.checkpoint.helper.PrefManager;
import red.point.checkpoint.ui.account.AccountFragment;
import red.point.checkpoint.ui.attendance.attend.AttendFragment;
import red.point.checkpoint.ui.home.HomeFragment;
import red.point.checkpoint.ui.pin.drop.DropPinFragment;
import red.point.checkpoint.ui.registration.PhoneAuthActivity;
import red.point.checkpoint.ui.registration.referral.ReferralFragment;
import red.point.checkpoint.ui.reward.RewardFragment;
import red.point.checkpoint.ui.wallet.WalletFragment;
import red.point.checkpoint.util.DateUtil;
import red.point.checkpoint.util.ProgressDialogUtil;
import red.point.checkpoint.util.ToastUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static red.point.checkpoint.ui.LoginActivity.mGoogleApiClient;

public class MainActivity extends BaseActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Unbinder unbinder;

    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private Fragment fragment;

    @Inject
    PrefManager prefManager;

    @Inject
    UserService userService;

    @Inject
    ReferralService referralService;

    @Inject
    UserService updateUserService;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private Call<UserResponse> userResponseCall;
    private Call<UserResponse> updateUserResponseCall;
    private Call<ReferralChecking> referralCheckingCall;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, "en"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        unbinder = ButterKnife.bind(this);

        MainActivityComponent mainActivityComponent = DaggerMainActivityComponent.builder()
                .contextModule(new ContextModule(MainActivity.this))
                .mainActivityModule(new MainActivityModule())
                .build();

        mainActivityComponent.inject(this);

        checkVersionCode();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        setSupportActionBar(toolbar);

        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            getSupportActionBar().setLogo(R.drawable.logo_header);
            getSupportActionBar().setDisplayUseLogoEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        checkAuth();

        FirebaseMessaging.getInstance().subscribeToTopic("news");
        FirebaseMessaging.getInstance().subscribeToTopic("company_" + prefManager.getCompanyId());
        
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        userResponseCall = userService.getUser(prefManager.getUserId());
        userResponseCall.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful()) {
                    User user = response.body().getUser();

                    updateUserResponseCall = updateUserService.updateUser(
                            user.getId(),
                            user.getName(),
                            user.getEmail(),
                            user.getPhone(),
                            refreshedToken
                    );

                    updateUserResponseCall.enqueue(new Callback<UserResponse>() {
                        @Override
                        public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                            if (response.body().getUser().getPhone() == null && prefManager.getSkipPhoneAuth() < DateUtil.getCurrentTimeInMillis()) {
                                Intent intent = new Intent(MainActivity.this, PhoneAuthActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                referralCheckingCall = referralService.isHaveReferral(prefManager.getUserId());
                                referralCheckingCall.enqueue(new Callback<ReferralChecking>() {
                                    @Override
                                    public void onResponse(Call<ReferralChecking> call, Response<ReferralChecking> response) {
                                        if (response.isSuccessful()) {
                                            if (!response.body().isHaveReferral()) {
                                                Fragment fragment = new ReferralFragment();
                                                FragmentManager fragmentManager = getSupportFragmentManager();
                                                fragmentManager.popBackStackImmediate(getClass().getSimpleName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
                                                FragmentTransaction transaction = fragmentManager.beginTransaction();
                                                transaction.replace(R.id.mainContent, fragment).commitNow();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ReferralChecking> call, Throwable t) {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onFailure(Call<UserResponse> call, Throwable t) {

                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {

            }
        });

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        if (savedInstanceState == null) {
            Fragment fragment = new HomeFragment();
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.popBackStackImmediate(getClass().getSimpleName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.mainContent, fragment).commitNow();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);

        final MenuItem menuItem = menu.findItem(R.id.notification);

        View actionView = menuItem.getActionView();

        TextView textCartItemCount = actionView.findViewById(R.id.badge);
        int mCartItemCount = 0;

        if (textCartItemCount != null) {
            if (mCartItemCount == 0) {
                if (textCartItemCount.getVisibility() != View.GONE) {
                    textCartItemCount.setVisibility(View.GONE);
                }
            } else {
                textCartItemCount.setText(String.valueOf(Math.min(mCartItemCount, 99)));
                if (textCartItemCount.getVisibility() != View.VISIBLE) {
                    textCartItemCount.setVisibility(View.VISIBLE);
                }
            }
        }

        actionView.setOnClickListener(v -> onOptionsItemSelected(menuItem));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.notification:
                ToastUtil.show("You don't have any notification yet");
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if(fragment instanceof WalletFragment) {
                    fragment.onActivityResult(requestCode, resultCode, data);
                }
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            getSupportActionBar().setLogo(R.drawable.logo_header);
            getSupportActionBar().setDisplayUseLogoEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        MyApplication.getInstance().setActivity(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        MyApplication.getInstance().setActivity(null);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }

        if (mGoogleApiClient != null) {
            if (mGoogleApiClient.isConnected()) {
                mGoogleApiClient.disconnect();
            }
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        ProgressDialogUtil.dismiss();

        unbinder.unbind();

        if (userResponseCall != null && userResponseCall.isExecuted()) userResponseCall.cancel();
        if (updateUserResponseCall != null && updateUserResponseCall.isExecuted()) updateUserResponseCall.cancel();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            getSupportActionBar().setLogo(R.drawable.logo_header);
            getSupportActionBar().setDisplayUseLogoEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        int backStackCount = getSupportFragmentManager().getBackStackEntryCount();

        if (backStackCount > 0) {
            // PopBackStack only if they have tag
            // This behaviour needed because every fragment need to be added
            // To the back stack to prevent overlapping when back button pressed
            for(int i = backStackCount - 1; i >= 0; i--) {
                FragmentManager.BackStackEntry backStackEntry = getSupportFragmentManager().getBackStackEntryAt(i);
                if (backStackEntry.getName() != null) {
                    getSupportFragmentManager().popBackStack(backStackEntry.getName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    break;
                }
            }
        } else {
            super.onBackPressed();
        }
    }

    private void clearBackStack() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            FragmentManager.BackStackEntry entry = getSupportFragmentManager().getBackStackEntryAt(0);
            getSupportFragmentManager().popBackStack(entry.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
            getSupportFragmentManager().executePendingTransactions();
        }
    }

    private OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.nav_home:
                    fragment = new HomeFragment();
                    break;
                case R.id.nav_attendance:
                    fragment = new AttendFragment();
                    break;
                case R.id.nav_drop_pin:
                    fragment = new DropPinFragment();
                    break;
                case R.id.nav_reward:
                    fragment = new RewardFragment();
                    break;
                case R.id.nav_account:
                    fragment = new AccountFragment();
                    break;
            }

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.mainContent, fragment);
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.mainContent);
            if (currentFragment != null && currentFragment.getClass() != fragment.getClass()) {
                transaction.addToBackStack(fragment.getClass().getSimpleName());
                transaction.setCustomAnimations(R.anim.slide_in, R.anim.slide_out, R.anim.back_in, R.anim.back_out);
            }
            transaction.commit();
            return true;
        }

    };

    private void checkAuth() {

        DatabaseReference userLastOnlineRef = FirebaseDatabase.getInstance().getReference("users").child(prefManager.getFirebaseUserId()).child("lastOnline");
        userLastOnlineRef.onDisconnect().setValue(ServerValue.TIMESTAMP);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        authListener = firebaseAuth -> {
            user = firebaseAuth.getCurrentUser();
            if (user == null) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        };
    }

    private void checkVersionCode() {
        final int versionCode = BuildConfig.VERSION_CODE;
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("minVersionCode");
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (versionCode < Integer.parseInt(dataSnapshot.getValue().toString())) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setIcon(R.drawable.ic_warning_24dp)
                            .setMessage("New update is available, please update your app now")
                            .setPositiveButton("Update", (dialog, whichButton) -> {
                                final String appPackageName = getPackageName();
                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                } catch (android.content.ActivityNotFoundException anfe) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                }
                                finishAffinity();
                            })
                            .setCancelable(false)
                            .show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void logout() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            auth.signOut();
            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                    status -> {
                        Toast.makeText(getApplicationContext(),"Logged Out", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                        startActivity(intent);
                    });
        }
    }
}
