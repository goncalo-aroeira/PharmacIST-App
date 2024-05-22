package pt.ulisboa.tecnico.cmov.pharmacist.domain;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

public class UserLocalStore {

    private static final String PREF_NAME = "LoginPreferences";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_NAME = "name";
    private static final String KEY_PASSWORD = "password";

    private static final String KEY_ID = "id";

    private static final String KEY_IS_LOGGEDIN = "isLoggedIn";

    SharedPreferences sharedPreferences;

    public UserLocalStore(@NonNull Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveLoginDetails(String id, String name, String email, String password) {
        sharedPreferences.edit().putString(KEY_ID, id).apply();
        sharedPreferences.edit().putString(KEY_NAME, name).apply();
        sharedPreferences.edit().putString(KEY_EMAIL, email).apply();
        sharedPreferences.edit().putString(KEY_PASSWORD, password).apply();
        sharedPreferences.edit().putBoolean(KEY_IS_LOGGEDIN, true).apply();
    }

    public void clearLoginDetails() {
        sharedPreferences.edit().clear().apply();
        sharedPreferences.edit().putBoolean(KEY_IS_LOGGEDIN, false).apply();
    }

    public String getLoggedInEmail() {
        return sharedPreferences.getString(KEY_EMAIL, "");
    }

    public String getLoggedInName() {
        return sharedPreferences.getString(KEY_NAME, "");
    }

    public String getLoggedInPassword() {
        return sharedPreferences.getString(KEY_PASSWORD, "");
    }

    public boolean isUserLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGEDIN, false);
    }

    public String getLoggedInId() {
        return sharedPreferences.getString(KEY_ID, "");
    }
    ;

}
