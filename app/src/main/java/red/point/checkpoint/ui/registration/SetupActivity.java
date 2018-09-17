package red.point.checkpoint.ui.registration;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import red.point.checkpoint.MyApplication;
import red.point.checkpoint.R;
import red.point.checkpoint.helper.PrefManager;
import red.point.checkpoint.ui.BaseActivity;
import red.point.checkpoint.ui.LoginActivity;
import red.point.checkpoint.ui.registration.setup.SetupCompanyFragment;
import red.point.checkpoint.ui.registration.setup.SetupFragment;
import red.point.checkpoint.util.ProgressDialogUtil;

import static red.point.checkpoint.ui.LoginActivity.mGoogleApiClient;

public class SetupActivity extends BaseActivity {

    private static final String TAG = SetupActivity.class.getSimpleName();
    private PrefManager prefManager;
    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_setup);

        auth = FirebaseAuth.getInstance();

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

            }
        };

        prefManager = new PrefManager(MyApplication.getAppContext());

        SetupFragment fragment = new SetupFragment();
        fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStackImmediate(fragment.getClass().getName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.mainContent, fragment).addToBackStack(fragment.getClass().getName()).commit();

        // Making notification bar transparent
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {

            for (Fragment fragment : fragments) {
                if(fragment instanceof SetupCompanyFragment) {
                    fragment.onActivityResult(requestCode, resultCode, data);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            finish();
        } else {
            super.onBackPressed();
        }
    }

    // We're being destroyed. It's important to dispose of the helper here!
    @Override
    public void onDestroy() {
        super.onDestroy();

        ProgressDialogUtil.dismiss();
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
    protected void onResume() {
        super.onResume();

        MyApplication.getInstance().setActivity(this);
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
    public void onPause() {
        MyApplication.getInstance().setActivity(null);

        super.onPause();
    }

    public void logout() {
        if (mGoogleApiClient != null) {
            if (mGoogleApiClient.isConnected()) {
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
}
