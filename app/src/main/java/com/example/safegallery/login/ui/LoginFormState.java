package com.example.safegallery.login.ui;

import androidx.annotation.Nullable;

/**
 * Data validation state of the login form.
 */
public class LoginFormState {

    @Nullable
    private final Integer passwordError;
    private final boolean isDataValid;

    public LoginFormState(@Nullable Integer passwordError) {
        this.passwordError = passwordError;
        this.isDataValid = false;
    }

    public LoginFormState(boolean isDataValid) {
        this.passwordError = null;
        this.isDataValid = isDataValid;
    }

    @Nullable
    public Integer getPasswordError() {
        return passwordError;
    }

    public boolean isDataValid() {
        return isDataValid;
    }
}
