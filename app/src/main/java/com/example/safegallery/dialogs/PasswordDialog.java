package com.example.safegallery.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.example.safegallery.Constants;
import com.example.safegallery.R;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class PasswordDialog extends DialogFragment {

    SharedPreferences globalSharedPreferences;
    SharedPreferences.Editor sharedPreferencesEditor;

    Context context;
    EditText etPassword;
    EditText etHint;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_password_dialog, null);

        this.init(view);

        builder.setView(view)
                .setTitle("Password update")
                .setPositiveButton("Update", null)
                .setNegativeButton("Don't show again", (dialog, which) -> this.onCloseForever())
                .setNeutralButton("Skip", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialog1 -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(v -> {
                if (onPasswordUpdate()) {
                    dialog.dismiss();
                    Toast.makeText(this.context, "Password updated successfully.", Toast.LENGTH_SHORT).show();
                }
            });
        });

        return dialog;
    }

    private void init(View view) {
        this.etPassword = view.findViewById(R.id.tlPassword);
        this.etHint = view.findViewById(R.id.etHint);

        this.globalSharedPreferences = this.context.getSharedPreferences(Constants.GLOBAL_SHARED_PREFS, Context.MODE_PRIVATE);
        this.sharedPreferencesEditor = this.globalSharedPreferences.edit();
    }

    private boolean onPasswordUpdate() {
        InputStatus inputStatus = this.validateInput();

        if (inputStatus.isValid()) {
            this.sharedPreferencesEditor.putString(Constants.PASSWORD, this.etPassword.getText().toString());
            this.sharedPreferencesEditor.putBoolean(Constants.SHOW_PASSWORD_DIALOG, false);

            if (this.etHint.getText().toString().trim().length() != 0)
                this.sharedPreferencesEditor.putString(Constants.PASSWORD_HINT, this.etHint.getText().toString());

            this.sharedPreferencesEditor.apply();
        } else
            this.etPassword.setError(inputStatus.getPasswordError());

        return inputStatus.isValid();
    }

    private void onCloseForever() {
        this.sharedPreferencesEditor.putBoolean(Constants.SHOW_PASSWORD_DIALOG, false);
        this.sharedPreferencesEditor.apply();
    }

    private InputStatus validateInput() {
        InputStatus status = new InputStatus();
        String password = etPassword.getText().toString().trim();

        if (password.length() == 0) {
            status.setPasswordError("Password is required");
            status.setValid(false);
        } else if (password.length() < 3) {
            status.setPasswordError("Password is too short");
            status.setValid(false);
        }

        return status;
    }

    @Getter
    @Setter
    private static class InputStatus {
        private boolean valid = true;
        private String passwordError = "";
    }
}
