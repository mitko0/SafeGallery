package com.example.safegallery.dialogs.interfaces;

import com.example.safegallery.tabs.data.DataEncryptorTask;
import com.example.safegallery.tabs.data.DataPath;

import java.util.List;

public interface ProgressListener {
    void onTaskFinish(List<DataPath> encryptedFiles, List<DataEncryptorTask.ErrorHolder> errorHolders);

    void onProgressUpdate(Integer ...values);

    void onPositiveButtonClick();

    void onNegativeButtonClick();
}
