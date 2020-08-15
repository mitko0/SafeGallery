package com.example.safegallery.recycler_views.interfaces;

import com.example.safegallery.tabs.data.DataEncryptorTask;

import java.util.List;

public interface ViewModelListener {
    void updateData(List<DataEncryptorTask.DataHolder> data);
}
