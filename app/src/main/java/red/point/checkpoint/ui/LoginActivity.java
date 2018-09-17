package red.point.checkpoint.ui;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.GoogleAuthProvider;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import red.point.checkpoint.BuildConfig;
import red.point.checkpoint.MyApplication;
import red.point.checkpoint.R;
import red.point.checkpoint.api.model.DeviceResponse;
import red.point.checkpoint.api.model.LoginResult;
import red.point.checkpoint.api.service.AuthService;
import red.point.checkpoint.api.service.UserDeviceService;
import red.point.checkpoint.di.ContextModule;
import red.point.checkpoint.helper.PrefManager;
import red.point.checkpoint.ui.registration.SetupActivity;
import red.point.checkpoint.util.ProgressDialogUtil;
import red.point.checkpoint.util.ToastUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 9002; // any value

    @Inject PrefManager prefManager;
    @Inject AuthService authService;
    @Inject UserDeviceService userDeviceService;

    @BindView(R.id.sign_in_button) SignInButton btnSignIn;

    public static GoogleApiClient mGoogleApiClient;

    private Unbinder unbinder;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private Call<LoginResult> loginResultCall;
    private Call<DeviceResponse> deviceResponseCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        unbinder = ButterKnife.bind(this);

        LoginComponent loginComponent = DaggerLoginComponent.builder()
                .contextModule(new ContextModule(this))
                .build();

        loginComponent.inject(this);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        btnSignIn.setSize(SignInButton.SIZE_WIDE);
        btnSignIn.setOnClickListener(v -> {
            Log.d(TAG, "Loading on click sign in");
            ProgressDialogUtil.showLoading(this);
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

        btnSignIn.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "Loading on resume");
        ProgressDialogUtil.showLoading(this);

        if (auth.getCurrentUser() != null) {
            Log.d(TAG, "authenticated");
            auth.getCurrentUser()
                    .getIdToken(false)
                    .addOnSuccessListener(getTokenResult -> login(getTokenResult.getToken()) )
                    .addOnFailureListener(task -> auth.signOut());
        } else {
            Log.d(TAG, "unauthenticated");
            btnSignIn.setVisibility(View.VISIBLE);
            Log.d(TAG, "Loading dismiss on resume");
            ProgressDialogUtil.dismiss();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        MyApplication.getInstance().setActivity(this);
    }

    @Override
    protected void onPause() {
        MyApplication.getInstance().setActivity(null);

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unbinder.unbind();
        if (loginResultCall != null && loginResultCall.isExecuted()) loginResultCall.cancel();

        ProgressDialogUtil.dismiss();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            finish();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                if (account != null) {
                    Log.d(TAG, "Loading on activity result ");
                    ProgressDialogUtil.showLoading(this);
                    firebaseAuthWithGoogle(account);
                }
            } else {
                ProgressDialogUtil.dismiss();
                // Google Sign In failed, update UI appropriately
                ToastUtil.show("Login cancelled");
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "Loading on firebase auth with google");
        ProgressDialogUtil.showLoading(this);
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential).addOnCompleteListener(this, signInCompleteListener);
    }

    private OnCompleteListener<AuthResult> signInCompleteListener = new OnCompleteListener<AuthResult>() {
        @Override
        public void onComplete(@NonNull Task<AuthResult> task) {
            if (task.isSuccessful()) {
                user = auth.getCurrentUser();
                ProgressDialogUtil.setMessage("Fetching User Data");
                if (user != null) {
                    user.getIdToken(true).addOnCompleteListener(getTokenCompleteListener);
                }
            } else {
                // If sign in fails, display a message to the user.
                ToastUtil.show("Authentication failed.");
                Log.d(TAG, "Loading on sign in complete listener");
                ProgressDialogUtil.dismiss();
            }
        }
    };

    private OnCompleteListener<GetTokenResult> getTokenCompleteListener = new OnCompleteListener<GetTokenResult>() {
        @Override
        public void onComplete(@NonNull Task<GetTokenResult> task) {
            if (task.isSuccessful()) {
                String idToken = task.getResult().getToken();
                // Send token to your backend via HTTPS
                prefManager.setTokenId(idToken);
                prefManager.setFirebaseUserId(user.getUid());
                prefManager.setUserEmail(user.getEmail());
                prefManager.setUserName(user.getDisplayName());

                login(idToken);
            } else {
                Log.e(TAG, String.valueOf(task.getException()));

                ToastUtil.show("Authentication failed.");

                ProgressDialogUtil.dismiss();
                Log.d(TAG, "Loading on get token complete listener");
            }
        }
    };

    private void login(String idToken) {
        ProgressDialogUtil.showLoading(this);
        Log.d(TAG, "Loading on login");

        loginResultCall = authService.login(idToken);
        loginResultCall.enqueue(new Callback<LoginResult>() {
            @Override
            public void onResponse(Call<LoginResult> call, Response<LoginResult> response) {
                // Sign in success, update UI with the signed-in user's information
                if (response.isSuccessful()) {
                    Intent intent = null;
                    LoginResult loginResult = response.body();
                    Log.d(TAG, "Login success");

                    prefManager.setUserId(loginResult.getUser().getId());
                    prefManager.setUserName(loginResult.getUser().getName());
                    prefManager.setUserEmail(loginResult.getUser().getEmail());

                    if (loginResult.getCompanies().size() > 0) {
                        for (int i = 0; i < loginResult.getCompanies().size(); i++) {
                            prefManager.setCompanyId(loginResult.getCompanies().get(i).getId());
                            prefManager.setCompanyName(loginResult.getCompanies().get(i).getName());

                            intent = new Intent(LoginActivity.this, MainActivity.class);
                        }
                    } else {
                        intent = new Intent(LoginActivity.this, SetupActivity.class);
                    }
                    
                    prefManager.setToken(loginResult.getToken());

                    deviceResponseCall = userDeviceService.storeDevice(
                        prefManager.getUserId(),
                        Build.BRAND,
                        Build.MODEL,
                        Build.MANUFACTURER,
                        "",
                        BuildConfig.VERSION_CODE,
                        BuildConfig.VERSION_NAME
                    );

                    deviceResponseCall.enqueue(new Callback<DeviceResponse>() {
                        @Override
                        public void onResponse(Call<DeviceResponse> call, Response<DeviceResponse> response) {

                        }

                        @Override
                        public void onFailure(Call<DeviceResponse> call, Throwable t) {

                        }
                    });

                    ProgressDialogUtil.dismiss();
                    Log.d(TAG, "Loading dismiss on login");

                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    btnSignIn.setVisibility(View.VISIBLE);
                    ProgressDialogUtil.dismiss();
                    Log.d(TAG, "Loading dismiss on failed login");
                }
            }

            @Override
            public void onFailure(Call<LoginResult> call, Throwable t) {
                ProgressDialogUtil.dismiss();
                Log.d(TAG, "Loading dismiss on failure login");
                if (! call.isCanceled()) {
                    ToastUtil.show("Login Failure");
                    Log.d(TAG, "Login Failure");
                    btnSignIn.setVisibility(View.VISIBLE);
                    auth.signOut();
                }
            }
        });
    }
}
