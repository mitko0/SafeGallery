package com.example.safegallery.login.data;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import com.example.safegallery.Constants;

import java.io.IOException;

/**
 * Class that handles authentication.
 */
public class LoginDataSource {

    Activity activity;

    public LoginDataSource(Activity activity) {
        this.activity = activity;
    }

    public Result login(String password) {

        SharedPreferences sharedPref = this.activity.getSharedPreferences(Constants.GLOBAL_SHARED_PREFS, Context.MODE_PRIVATE);
        String spPassword = sharedPref.getString(Constants.PASSWORD, Constants.DEFAULT_PASSWORD);
        if (password.equals(spPassword))
            return new Result.Success<>(password);

        return new Result.Error(new IOException("Error logging in"));
    }
}
