package pt.ulisboa.tecnico.cmov.pharmacist.domain;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

public class UserLocalStore {

    // Class for storing user data on the phone (shared preferences) and get logged in users details

    public static final String SP_NAME = "userDetails";

    SharedPreferences userLocalDatabase;


    public UserLocalStore(@NonNull Context context) {
        // force activities to give us their context
        userLocalDatabase = context.getSharedPreferences(SP_NAME, 0);
    }

    //save user as logged in
    public void storeUserData(@NonNull User user) {
        SharedPreferences.Editor spEditor = userLocalDatabase.edit();
        spEditor.putString("name", user.name);
        spEditor.putString("username", user.username);
        spEditor.putString("password", user.password);
        spEditor.apply();
        spEditor.commit();
    }

    // return the user that logged in
    public User getLoggedInUser() {
        String name = userLocalDatabase.getString("name", "");
        String username = userLocalDatabase.getString("username", "");
        String password = userLocalDatabase.getString("password", "");

        return new User(name, username, password);
    }

    // return boolean to verify in user in logged in yet
    public boolean getUserLoggedIn() {
        if (userLocalDatabase.getBoolean("loggedIn", false) == true) {
            return true;
        } else {
            return false;
        }
    }

    public void setUserLoggedIn(boolean loggedIn) {
        SharedPreferences.Editor spEditor = userLocalDatabase.edit();
        spEditor.putBoolean("loggedIn", loggedIn);
        spEditor.apply();
        spEditor.commit();
    }

    public void cleanUserData() {
        SharedPreferences.Editor spEditor = userLocalDatabase.edit();
        spEditor.clear();
        spEditor.apply();
        spEditor.commit();
    }

}
