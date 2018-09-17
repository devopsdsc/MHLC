package red.point.checkpoint.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

public class PrefManager {
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context _context;

    // shared pref mode
    private final static int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "CHECKPOINT";
    private static final String IS_FIRST_TIME_LAUNCH_REPORT = "IS_FIRST_TIME_LAUNCH_REPORT";
    private static final String IS_FIRST_TIME_SETUP = "IS_FIRST_TIME_SETUP";
    private static final String IS_FIRST_TIME_SEE_WALLET = "IS_FIRST_TIME_SEE_WALLET";
    private static final String FIREBASE_USER_ID = "FIREBASE_USER_ID";
    private static final String USER_ID = "USER_ID";
    private static final String USER_NAME = "USER_NAME";
    private static final String USER_EMAIL = "USER_EMAIL";
    private static final String COMPANY_ID = "COMPANY_ID";
    private static final String FIREBASE_COMPANY_ID = "FIREBASE_COMPANY_ID";
    private static final String COMPANY_NAME = "COMPANY_NAME";
    private static final String TOKEN_ID = "TOKEN_ID";
    private static final String TOKEN = "TOKEN";
    private static final String SKIP_PHONE_AUTH = "SKIP_PHONE_AUTH";

    @SuppressLint("CommitPrefEdits")
    public PrefManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setSkipPhoneAuth(long timestamp) {
        editor.putLong(SKIP_PHONE_AUTH, timestamp);
        editor.commit();
    }

    public Long getSkipPhoneAuth() {
        return pref.getLong(SKIP_PHONE_AUTH, 0);
    }

    public void setToken(String token) {
        editor.putString(TOKEN, token);
        editor.commit();
    }

    public void setTokenId(String token) {
        editor.putString(TOKEN_ID, token);
        editor.commit();
    }

    public void setCompanyId(long companyId) {
        editor.putLong(COMPANY_ID, companyId);
        editor.commit();
    }

    public void setUserId(long userId) {
        editor.putLong(USER_ID, userId);
        editor.commit();
    }

    public void setFirstTimeLaunchReport(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH_REPORT, isFirstTime);
        editor.commit();
    }

    public void setFirstTimeSetup(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_SETUP, isFirstTime);
        editor.commit();
    }

    public void setFirstTimeSeeWallet(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_SEE_WALLET, isFirstTime);
        editor.commit();
    }

    public void setFirebaseUserId(String userId) {
        editor.putString(FIREBASE_USER_ID, userId);
        editor.commit();
    }

    public void setUserName(String userName) {
        editor.putString(USER_NAME, userName);
        editor.commit();
    }

    public void setUserEmail(String userEmail) {
        editor.putString(USER_EMAIL, userEmail);
        editor.commit();
    }

    public void setFirebaseCompanyId(String firebaseCompanyId) {
        editor.putString(FIREBASE_COMPANY_ID, firebaseCompanyId);
        editor.commit();
    }

    public void setCompanyName(String companyName) {
        editor.putString(COMPANY_NAME, companyName);
        editor.commit();
    }

    public boolean isFirstTimeLaunchReport() {
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH_REPORT, true);
    }

    public boolean isFirstTimeSetup() {
        return pref.getBoolean(IS_FIRST_TIME_SETUP, true);
    }

    public boolean isFirstTimeSeeWallet() {
        return pref.getBoolean(IS_FIRST_TIME_SEE_WALLET, true);
    }


    public String getFirebaseUserId() {
        return pref.getString(FIREBASE_USER_ID, "");
    }

    public String getUserName() {
        return pref.getString(USER_NAME, "");
    }

    public String getUserEmail() {
        return pref.getString(USER_EMAIL, "");
    }

    public String getFirebaseCompanyId() {
        return pref.getString(FIREBASE_COMPANY_ID, "");
    }

    public long getCompanyId() {
        return pref.getLong(COMPANY_ID, 0);
    }

    public long getUserId() {
        return pref.getLong(USER_ID, 0);
    }

    public String getCompanyName() {
        return pref.getString(COMPANY_NAME, "");
    }

    public String getToken() {
        return pref.getString(TOKEN, "");
    }

    public String getTokenId() {
        return pref.getString(TOKEN_ID, "");
    }

}
