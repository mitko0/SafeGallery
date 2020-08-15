package com.example.safegallery.dialogs.interfaces;

import com.example.safegallery.tabs.data.DataEncryptorTask;

import java.util.List;

public interface ProgressListener {
    void onTaskFinish(List<DataEncryptorTask.DataHolder> encryptedFiles, List<DataEncryptorTask.DataHolder> errorHolders);

    void onProgressUpdate(Integer ...values);

    void onPositiveButtonClick();

    void onNegativeButtonClick();
}
