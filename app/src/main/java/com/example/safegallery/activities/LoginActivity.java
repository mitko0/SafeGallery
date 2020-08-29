package com.example.safegallery.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.drawable.GradientDrawable;
import android.hardware.biometrics.BiometricManager;
import android.hardware.biometrics.BiometricPrompt;
import android.hardware.camera2.*;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.icu.text.SimpleDateFormat;
import android.media.Image;
import android.media.ImageReader;
import android.os.*;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import com.example.safegallery.Constants;
import com.example.safegallery.R;
import com.example.safegallery.login.ui.LoginViewModel;
import com.example.safegallery.login.ui.LoginViewModelFactory;
import com.example.safegallery.tabs.data.StorageData;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Date;

public class LoginActivity extends AppCompatActivity {

    private static final int PERMISSION_CODE = 1;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 270);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 90);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    String cameraId;
    CameraDevice cameraDevice;
    CameraManager cameraManager;
    ImageReader imageReader;
    CameraCaptureSession cameraCaptureSession;
    CaptureRequest.Builder captureRequestBuilder;
    Size imageDimensions;
    File file;
    Handler mBackgroundHandler;
    HandlerThread mBackgroundThread;

    SharedPreferences globalSharedPreferences;
    TextInputLayout tlPassword;
    TextInputEditText etPassword;
    ProgressBar loadingProgressBar;
    ImageButton ibFingerprint;

    private LoginViewModel loginViewModel;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.AuthenticationCallback authenticationCallback;

    CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            try {
                initCaptureSession();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Constants.revokeAppPermissions(this, PERMISSION_CODE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.USE_BIOMETRIC);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setBackgroundDrawable(getDrawable(R.drawable.app_bar));

        this.init();
        this.setStoredPassword();
        this.checkBiometricSupport();
        this.registerListeners();
    }

    @Override
    protected void onResume() {
        this.startBackgroundThread();
        try {
            this.openCamera();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        try {
            this.stopBackgroundThread();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_CODE) {
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_DENIED)
                    this.finish();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void init() {
        this.cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        this.globalSharedPreferences = this.getSharedPreferences(Constants.GLOBAL_SHARED_PREFS, MODE_PRIVATE);

        this.tlPassword = findViewById(R.id.tlPassword);
        this.etPassword = findViewById(R.id.etPassword);
        this.loadingProgressBar = findViewById(R.id.loading);
        this.ibFingerprint = findViewById(R.id.ibFingerprint);

        this.loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory(this))
                .get(LoginViewModel.class);

        this.biometricPrompt = new BiometricPrompt.Builder(this)
                .setTitle("Biometric Login")
                .setSubtitle("Authentication is required to continue")
                .setDescription("This app uses biometric authentication to protect your data.")
                .setNegativeButton("Cancel", this.getMainExecutor(), (dialogInterface, i) -> {
                })
                .build();

        this.authenticationCallback = new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                handleLoginSuccessful();
                super.onAuthenticationSucceeded(result);
            }

            @Override
            public void onAuthenticationFailed() {
                handleLoginFailed();
                super.onAuthenticationFailed();
            }
        };
    }

    private void openCamera() throws CameraAccessException {
        if (this.cameraManager != null) {
            this.cameraId = this.cameraManager.getCameraIdList()[1];
            CameraCharacteristics characteristics = this.cameraManager.getCameraCharacteristics(this.cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (map != null)
                this.imageDimensions = map.getOutputSizes(ImageReader.class)[0];

            this.cameraManager.openCamera(this.cameraId, this.stateCallback, this.mBackgroundHandler);
        }
    }

    private void initCaptureSession() throws CameraAccessException {
        this.imageReader = ImageReader.newInstance(this.imageDimensions.getWidth(), this.imageDimensions.getHeight(), ImageFormat.JPEG, 1);
        Surface surface = this.imageReader.getSurface();

        this.captureRequestBuilder = this.cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);

        this.captureRequestBuilder.addTarget(surface);
        this.cameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession session) {
                if (cameraDevice == null)
                    return;

                cameraCaptureSession = session;
                captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                Toast.makeText(LoginActivity.this, "Camera configuration failed", Toast.LENGTH_SHORT).show();
            }
        }, this.mBackgroundHandler);

        this.imageReader.setOnImageAvailableListener(reader -> {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
            Date date = new Date();

            this.file = new File(String.format("%s%s.jpg", StorageData.INTRUDERS_FOLDER, formatter.format(date)));
            try (Image image = this.imageReader.acquireLatestImage()) {
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.capacity()];
                buffer.get(bytes);
                this.saveCapture(bytes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, this.mBackgroundHandler);
    }

    private void registerListeners() {

        this.loginViewModel.getLoginResult().observe(this, loginResult -> {
            if (loginResult == null) return;

            this.loadingProgressBar.setVisibility(View.GONE);
            if (loginResult.getState() == R.string.login_successful)
                this.handleLoginSuccessful();
            else {
                this.handleLoginFailed();
            }
        });

        this.etPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                boolean loginOk = this.loginViewModel.login(v.getText().toString());
                if (!loginOk)
                    Toast.makeText(this, "Invalid password", Toast.LENGTH_SHORT).show();
                return !loginOk;
            }
            return false;
        });

        this.ibFingerprint.setOnClickListener(v -> this.biometricPrompt.authenticate(new CancellationSignal(), this.getMainExecutor(), this.authenticationCallback));

        this.tlPassword.setStartIconOnClickListener(v -> {
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

    private void handleLoginFailed() {
        if (cameraDevice == null)
            return;

        try {
            int rotation = this.getWindowManager().getDefaultDisplay().getRotation();
            this.captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
            this.cameraCaptureSession.capture(this.captureRequestBuilder.build(), null, this.mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void saveCapture(byte[] bytes) throws IOException {
        OutputStream outputStream = new FileOutputStream(this.file);
        outputStream.write(bytes);
        outputStream.close();
    }

    private void hideInputView() {
        View view = this.getCurrentFocus();
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);

        if (view != null && imm != null)
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void setStoredPassword() {
        SharedPreferences.Editor editor = this.globalSharedPreferences.edit();
        String password = this.globalSharedPreferences.getString(Constants.PASSWORD, null);
        if (password == null) {
            editor.putString(Constants.PASSWORD, Constants.DEFAULT_PASSWORD);
            editor.apply();
        }
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("camera background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    protected void stopBackgroundThread() throws InterruptedException {
        mBackgroundThread.quitSafely();
        mBackgroundThread.join();
        mBackgroundThread = null;
        mBackgroundHandler = null;
    }
}
