package com.example.safegallery.login.ui;

import android.app.Activity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.annotation.NonNull;

import com.example.safegallery.login.data.LoginDataSource;
import com.example.safegallery.login.data.LoginRepository;

/**
 * ViewModel provider factory to instantiate LoginViewModel.
 * Required given LoginViewModel has a non-empty constructor
 */
public class LoginViewModelFactory implements ViewModelProvider.Factory {

    Activity activity;

    public LoginViewModelFactory(Activity activity) {
        this.activity = activity;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(LoginViewModel.class)) {
            return (T) new LoginViewModel(LoginRepository.getInstance(new LoginDataSource(this.activity)));
        } else {
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
