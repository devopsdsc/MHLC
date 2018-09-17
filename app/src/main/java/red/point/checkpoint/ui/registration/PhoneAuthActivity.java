package red.point.checkpoint.ui.registration;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.heetch.countrypicker.CountryPickerDialog;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import red.point.checkpoint.R;
import red.point.checkpoint.api.model.User;
import red.point.checkpoint.api.model.UserResponse;
import red.point.checkpoint.api.service.UserService;
import red.point.checkpoint.di.ContextModule;
import red.point.checkpoint.helper.PrefManager;
import red.point.checkpoint.ui.MainActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PhoneAuthActivity extends AppCompatActivity {

    private static final String TAG = "PhoneAuthActivity";

    private Unbinder unbinder;

    private Activity mActivity;

    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";

    private static final int STATE_INITIALIZED = 1;
    private static final int STATE_CODE_SENT = 2;
    private static final int STATE_VERIFY_FAILED = 3;
    private static final int STATE_VERIFY_SUCCESS = 4;
    private static final int STATE_SIGNIN_FAILED = 5;
    private static final int STATE_SIGNIN_SUCCESS = 6;
    private static final int STATE_INVALID_PHONE_NUMBER = 7;

    private FirebaseAuth mAuth;

    private boolean mVerificationInProgress = false;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @BindView(R.id.detail) TextView mDetailText;
    @BindView(R.id.country) TextView mCountry;
    @BindView(R.id.field_phone_number) EditText mPhoneNumberField;
    @BindView(R.id.field_verification_code) EditText mVerificationField;

    @BindView(R.id.button_verify_phone) Button mVerifyButton;
    @BindView(R.id.button_resend) Button mResendButton;
    @BindView(R.id.button_start_verification) Button mStartButton;
    @BindView(R.id.button_change_phone_number) Button mChangePhoneNumberButton;
    @BindView(R.id.button_skip) Button mSkipButton;

    private Call<UserResponse> userResponseCall;
    private Call<UserResponse> updateUserResponseCall;

    @Inject
    PrefManager prefManager;

    @Inject
    UserService userService;

    @Inject
    UserService updateUserService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_auth);

        mActivity = this;

        unbinder = ButterKnife.bind(this);

        PhoneAuthComponent phoneAuthComponent = DaggerPhoneAuthComponent.builder()
                .contextModule(new ContextModule(PhoneAuthActivity.this))
                .phoneAuthModule(new PhoneAuthModule())
                .build();

        phoneAuthComponent.inject(this);

        // Restore instance state
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        }

        mCountry.setText("+62");

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]

        // Initialize phone auth callbacks
        // [START phone_auth_callbacks]
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verificaiton without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:" + credential);
                // [START_EXCLUDE silent]
                mVerificationInProgress = false;
                // [END_EXCLUDE]

                // [START_EXCLUDE silent]
                // Update the UI and attempt sign in with the phone credential
                updateUI(STATE_VERIFY_SUCCESS, credential);
                // [END_EXCLUDE]
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e);
                // [START_EXCLUDE silent]
                mVerificationInProgress = false;
                // [END_EXCLUDE]

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // [START_EXCLUDE]
                    mPhoneNumberField.setError("Invalid phone number.");
                    updateUI(STATE_INVALID_PHONE_NUMBER);
                    // [END_EXCLUDE]
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // [START_EXCLUDE]
                    Snackbar.make(findViewById(android.R.id.content), "Quota exceeded.",
                            Snackbar.LENGTH_SHORT).show();
                    updateUI(STATE_INVALID_PHONE_NUMBER);
                    // [END_EXCLUDE]
                } else {
                    // Show a message and update the UI
                    // [START_EXCLUDE]
                    updateUI(STATE_VERIFY_FAILED);
                    // [END_EXCLUDE]
                }
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                // [START_EXCLUDE]
                // Update UI
                updateUI(STATE_CODE_SENT);
                // [END_EXCLUDE]
            }
        };
        // [END phone_auth_callbacks]
    }

    @Override
    public void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);

        // [START_EXCLUDE]
        if (mVerificationInProgress && validatePhoneNumber()) {
            startPhoneNumberVerification("+" + mCountry.getText().toString() + mPhoneNumberField.getText().toString());
        }
        // [END_EXCLUDE]
        updateUI(STATE_INITIALIZED);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "phone on destroy");
        super.onDestroy();

        unbinder.unbind();

        // TODO: on destroy called in process, cancel this call cause phone not updated to database
        // if (userResponseCall != null && userResponseCall.isExecuted()) userResponseCall.cancel();
        // if (updateUserResponseCall != null && updateUserResponseCall.isExecuted()) updateUserResponseCall.cancel();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS);
    }

    @OnClick(R.id.country)
    public void chooseCountry() {
        CountryPickerDialog countryPicker =
                new CountryPickerDialog(mActivity, (country, flagResId) -> mCountry.setText(String.format("+%s", country.getDialingCode())));
        countryPicker.show();
    }

    @OnClick(R.id.button_start_verification)
    public void startVerification() {
        if (!validatePhoneNumber()) {
            return;
        }

        startPhoneNumberVerification("+" + mCountry.getText().toString() + mPhoneNumberField.getText().toString());
    }

    @OnClick(R.id.button_change_phone_number)
    public void changePhoneNumber() {
        updateUI(STATE_INITIALIZED);
    }

    @OnClick(R.id.button_verify_phone)
    public void verifyPhone() {
        String code = mVerificationField.getText().toString();
        if (TextUtils.isEmpty(code)) {
            mVerificationField.setError("Cannot be empty.");
        }
        verifyPhoneNumberWithCode(mVerificationId, code);
    }

    @OnClick(R.id.button_resend)
    public void resendVerificationCode(View v) {
        resendVerificationCode(mCountry.getText().toString() + mPhoneNumberField.getText().toString(), mResendToken);
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        // [START verify_with_code]
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        // [END verify_with_code]
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.getCurrentUser().linkWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");

                        mVerificationField.setError("Success.");

                        FirebaseUser user = task.getResult().getUser();
                        // [START_EXCLUDE]
                        updateUI(STATE_SIGNIN_SUCCESS, user);
                        // [END_EXCLUDE]

                        updateEmployeePhone();
                    } else {
                        // Sign in failed, display a message and update the UI
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            // The verification code entered was invalid
                            // [START_EXCLUDE silent]
                            mVerificationField.setError("Invalid code.");
                            // [END_EXCLUDE]
                        }

                        Log.d(TAG, "TASK " + task.getException().getMessage());
                        mDetailText.setText(String.format("%s", task.getException().getMessage()));

                        // [START_EXCLUDE silent]
                        // Update UI
                        updateUI(STATE_SIGNIN_FAILED);
                        // [END_EXCLUDE]
                    }
                });
    }

    private void updateEmployeePhone() {
        Log.d(TAG, "update phone employee");
        final String phone = mCountry.getText().toString() + mPhoneNumberField.getText().toString();
        Log.d(TAG, "update phone employee " + phone);
        Log.d(TAG, "update phone employee " + prefManager.getUserId());
        userResponseCall = userService.getUser(prefManager.getUserId());
        userResponseCall.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                Log.d(TAG, "update phone is " + response.isSuccessful());
                if (response.isSuccessful()) {
                    User user = response.body().getUser();
                    Log.d(TAG, "update phone user " + user.getId());
                    Log.d(TAG, "update phone user " + user.getName());
                    Log.d(TAG, "update phone user " + user.getEmail());
                    Log.d(TAG, "update phone user " + phone);
                    Log.d(TAG, "update phone user " + user.getFirebaseToken());
                    updateUserResponseCall = updateUserService.updateUser(
                            user.getId(),
                            user.getName(),
                            user.getEmail(),
                            phone,
                            user.getFirebaseToken()
                    );

                    updateUserResponseCall.enqueue(new Callback<UserResponse>() {
                        @Override
                        public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                            Log.d(TAG, "update phone success");
                            if (response.isSuccessful()) {
                                startActivity(new Intent(PhoneAuthActivity.this, MainActivity.class));
                                finish();
                            }
                        }

                        @Override
                        public void onFailure(Call<UserResponse> call, Throwable t) {
                            Log.d(TAG, "update phone failed " + t.getLocalizedMessage());
                        }
                    });
                } else {
                    Log.d(TAG, "update phone user else ");
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Log.d(TAG, "get user failed" + t.getLocalizedMessage());
            }
        });
    }

    private void startPhoneNumberVerification(String phoneNumber) {
        // [START start_phone_auth]
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]

        mVerificationInProgress = true;
    }

    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }

    private void updateUI(int uiState) {
        updateUI(uiState, mAuth.getCurrentUser(), null);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            updateUI(STATE_SIGNIN_SUCCESS, user);
        } else {
            updateUI(STATE_INITIALIZED);
        }
    }

    private void updateUI(int uiState, FirebaseUser user) {
        updateUI(uiState, user, null);
    }

    private void updateUI(int uiState, PhoneAuthCredential cred) {
        updateUI(uiState, null, cred);
    }

    private void updateUI(int uiState, FirebaseUser user, PhoneAuthCredential cred) {
        Log.d(TAG, uiState + " STATE");
        switch (uiState) {
            case STATE_INITIALIZED:
                // Initialized state, show only the phone number field and start button
                showViews(mStartButton, mPhoneNumberField, mSkipButton);
                enableViews(mStartButton, mPhoneNumberField);
                hideViews(mVerifyButton, mResendButton, mVerificationField, mChangePhoneNumberButton);
                mDetailText.setText(null);
                break;
            case STATE_CODE_SENT:
                // Code sent state, show the verification field, the
                showViews(mVerifyButton, mVerificationField, mChangePhoneNumberButton);
                disableViews(mPhoneNumberField);
                hideViews(mStartButton, mSkipButton);
                mDetailText.setText(R.string.status_code_sent);
                break;
            case STATE_VERIFY_FAILED:
                // Verification has failed, show all options
                showViews(mStartButton, mVerifyButton, mVerificationField, mSkipButton);
                hideViews(mResendButton);
                enableViews(mStartButton, mVerifyButton, mResendButton, mPhoneNumberField,
                        mVerificationField);
                mDetailText.setText(R.string.status_verification_failed);
                break;
            case STATE_VERIFY_SUCCESS:
                // Verification has succeeded, proceed to firebase sign in
                disableViews(mStartButton, mVerifyButton, mResendButton, mPhoneNumberField,
                        mVerificationField);
                mDetailText.setText(R.string.status_verification_succeeded);
                updateEmployeePhone();

                break;
            case STATE_INVALID_PHONE_NUMBER:
                showViews(mStartButton, mPhoneNumberField, mSkipButton);
                enableViews(mStartButton, mPhoneNumberField);
                hideViews(mVerifyButton, mResendButton, mVerificationField);
                mDetailText.setText(null);

                break;
            case STATE_SIGNIN_FAILED:
                // No-op, handled by sign-in check
                break;
            case STATE_SIGNIN_SUCCESS:
                // Np-op, handled by sign-in check
                break;
        }
    }

    private boolean validatePhoneNumber() {
        String phoneNumber = "+" + mCountry.getText().toString() + mPhoneNumberField.getText().toString();
        if (TextUtils.isEmpty(phoneNumber)) {
            mPhoneNumberField.setError("Invalid phone number.");
            return false;
        }

        return true;
    }

    private void enableViews(View... views) {
        for (View v : views) {
            v.setEnabled(true);
        }
    }

    private void disableViews(View... views) {
        for (View v : views) {
            v.setEnabled(false);
        }
    }

    private void showViews(View... views) {
        for (View v : views) {
            v.setVisibility(View.VISIBLE);
        }
    }

    private void hideViews(View... views) {
        for (View v : views) {
            v.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.button_skip)
    public void skip() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        prefManager.setSkipPhoneAuth(cal.getTimeInMillis());
        startActivity(new Intent(PhoneAuthActivity.this, MainActivity.class));
        finish();
    }

}