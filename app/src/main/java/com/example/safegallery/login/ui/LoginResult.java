package com.example.safegallery.login.ui;

import androidx.annotation.Nullable;

/**
 * Authentication result : success or error message.
 */
public class LoginResult {

    private final Integer state;

    public LoginResult(@Nullable Integer state) {
        this.state = state;
    }

    public Integer getState() {
        return this.state;
    }
}
