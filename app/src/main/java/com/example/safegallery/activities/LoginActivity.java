package com.example.safegallery.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.biometrics.BiometricManager;
import android.hardware.biometrics.BiometricPrompt;
import android.os.CancellationSignal;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import androidx.lifecycle.ViewModelProvider;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.example.safegallery.Constants;
import com.example.safegallery.R;
import com.example.safegallery.login.ui.LoginViewModel;
import com.example.safegallery.login.ui.LoginViewModelFactory;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class LoginActivity extends AppCompatActivity {

    SharedPreferences globalSharedPreferences;

    EditText etPassword;
    ProgressBar loadingProgressBar;
    ImageButton ibFingerprint;
    ImageButton ibHint;

    private LoginViewModel loginViewModel;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.AuthenticationCallback authenticationCallback;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Constants.revokeAppPermissions(this, 100,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.USE_BIOMETRIC);

        this.init();
        this.setStoredPassword();
        this.checkBiometricSupport();
        this.registerListeners();
    }

    private void init() {
        this.globalSharedPreferences = this.getSharedPreferences(Constants.GLOBAL_SHARED_PREFS, MODE_PRIVATE);

        this.etPassword = findViewById(R.id.etPassword);
        this.loadingProgressBar = findViewById(R.id.loading);
        this.ibFingerprint = findViewById(R.id.ibFingerprint);
        this.ibHint = findViewById(R.id.ibHint);

        this.loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory(this))
                .get(LoginViewModel.class);

        this.biometricPrompt = new BiometricPrompt.Builder(this)
                .setTitle("Biometric Login")
                .setSubtitle("Authentication is required to continue")
                .setDescription("This app uses biometric authentication to protect your data.")
                .setNegativeButton("Cancel", this.getMainExecutor(), (dialogInterface, i) -> {})
                .build();

        this.authenticationCallback = new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                handleLoginSuccessful();
                super.onAuthenticationSucceeded(result);
            }
        };
    }

    private void registerListeners() {

        this.loginViewModel.getLoginResult().observe(this, loginResult -> {
            if (loginResult == null) return;

            this.loadingProgressBar.setVisibility(View.GONE);
            if (loginResult.getState() == R.string.login_successful)
                this.handleLoginSuccessful();
        });

        this.etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loginViewModel.login(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // ignore
            }
        });

        this.ibFingerprint.setOnClickListener(v -> this.biometricPrompt.authenticate(new CancellationSignal(), this.getMainExecutor(), this.authenticationCallback));

        this.ibHint.setOnClickListener(v -> {
            String msg = this.globalSharedPreferences.getString(Constants.PASSWORD_HINT, "Initial password is 0000\n(Fingerprint unlock is an option as well)");

            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
            builder.setTitle("Password hint")
                    .setMessage(msg)
                    .setPositiveButton("Confirm", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }

    private void checkBiometricSupport() {
        BiometricManager biometricManager = (BiometricManager) getSystemService(BIOMETRIC_SERVICE);

        this.ibFingerprint.setVisibility(View.GONE);
        if (biometricManager != null) {
            switch (biometricManager.canAuthenticate()) {
                case BiometricManager.BIOMETRIC_SUCCESS:
                    this.ibFingerprint.setVisibility(View.VISIBLE);
                    break;
                case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                    Toast.makeText(this, "No biometric features available on this device.", Toast.LENGTH_SHORT).show();
                    break;
                case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                    Toast.makeText(this, "Biometric features are currently unavailable.", Toast.LENGTH_SHORT).show();
                    break;
                case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                    Toast.makeText(this, "The user hasn't associated any biometric credentials with their account.", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    private void handleLoginSuccessful() {
        this.loadingProgressBar.setVisibility(View.VISIBLE);
        this.hideInputView();

        boolean showModal = this.globalSharedPreferences.getBoolean(Constants.SHOW_PASSWORD_DIALOG, true);

        Intent intent = new Intent(this, TabbedActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.SHOW_PASSWORD_DIALOG, showModal);
        this.startActivity(intent);
        this.finish();
    }

    private void hideInputView() {
        View view = this.getCurrentFocus();
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);

        if (view != null && imm != null)
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void setStoredPassword() {
        SharedPreferences sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String password = sharedPreferences.getString(Constants.PASSWORD, null);
        if (password == null) {
            editor.putString(Constants.PASSWORD, Constants.DEFAULT_PASSWORD);
            editor.apply();
        }
    }
}
