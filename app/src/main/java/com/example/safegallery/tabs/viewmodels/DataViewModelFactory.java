package com.example.safegallery.tabs.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class DataViewModelFactory implements ViewModelProvider.Factory {

    Application application;

    public DataViewModelFactory(Application application) {
        this.application = application;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(DataViewModel.class)) {
            return (T) new DataViewModel(this.application);
        } else {
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
