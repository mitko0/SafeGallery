package com.example.safegallery.login.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.safegallery.R;
import com.example.safegallery.login.data.LoginRepository;
import com.example.safegallery.login.data.Result;

public class LoginViewModel extends ViewModel {

    private final MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private final MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private final LoginRepository loginRepository;

    public LoginViewModel(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
    }

    public LiveData<LoginFormState> getLoginFormState() {
        return this.loginFormState;
    }

    public LiveData<LoginResult> getLoginResult() {
        return this.loginResult;
    }

    public boolean login(String password) {
        Result result = this.loginRepository.login(password);

        if (result instanceof Result.Success) {
            this.loginResult.setValue(new LoginResult(R.string.login_successful));
            return true;
        }
        else
            this.loginResult.setValue(new LoginResult(R.string.login_failed));
        return false;
    }

    public void loginDataChanged(String password) {
        if (!isPasswordValid(password))
            this.loginFormState.setValue(new LoginFormState(R.string.invalid_password));
        else
            this.loginFormState.setValue(new LoginFormState(true));
    }

    // A placeholder password validation check
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() >= 4;
    }
}
