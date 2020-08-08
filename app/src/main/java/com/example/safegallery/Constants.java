package com.example.safegallery;

import android.app.Activity;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class Constants {

    public static final String GLOBAL_SHARED_PREFS = "global shared preferences";

    public static final String PASSWORD = "password";

    public static final String DEFAULT_PASSWORD = "0000";

    public static final String SHOW_PASSWORD_DIALOG = "show_password_dialog";

    public static final String PASSWORD_HINT = "password_hint";

    public static void revokeAppPermissions(Activity activity, int requestCode, @NonNull String... permissions) {
        List<String> requests = new ArrayList<>();

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED)
                requests.add(permission);
        }

        if (requests.size() != 0)
            ActivityCompat.requestPermissions(activity, requests.toArray(new String[0]), requestCode);
    }
}
