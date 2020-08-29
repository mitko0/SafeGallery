package com.example.safegallery.tabs.viewmodels;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.safegallery.Constants;
import com.example.safegallery.tabs.data.DataLoaderTask;
import com.example.safegallery.tabs.data.DataPath;
import com.example.safegallery.tabs.data.DataType;

import java.util.List;
import java.util.Map;

public class DataViewModel extends AndroidViewModel {

    private final Context context;
    private MutableLiveData<Map<String, List<DataPath>>>[] dataMaps;

    public DataViewModel(@NonNull Application application) {
        super(application);
        this.context = application;
    }

    public LiveData<Map<String, List<DataPath>>> getDataMap(int position) {
        return this.dataMaps[position];
    }

    @SuppressWarnings("unchecked")
    public void loadData(int position) {
        int len = DataType.values().length;

        if (this.dataMaps == null) {
            this.dataMaps = new MutableLiveData[Constants.TAB_LENGTH];
            for (int i = 0; i < Constants.TAB_LENGTH; i++)
                this.dataMaps[i] = new MutableLiveData<>();
        }

        if (this.dataMaps[position].getValue() == null) {
            new DataLoaderTask(this.context.getContentResolver(), position, data -> this.dataMaps[position].postValue(data))
                    .execute(DataType.values()[position % len]);
        }
    }

    @SuppressWarnings("unchecked")
    public void loadAllData() {
        int len = DataType.values().length;

        if (this.dataMaps == null) {
            this.dataMaps = new MutableLiveData[Constants.TAB_LENGTH];
            for (int i = 0; i < Constants.TAB_LENGTH; i++)
                this.dataMaps[i] = new MutableLiveData<>();
        }

        for (int i = 0; i < Constants.TAB_LENGTH; i++) {
            int position = i;
            if (this.dataMaps[position].getValue() == null) {
                new DataLoaderTask(this.context.getContentResolver(), position, data -> this.dataMaps[position].postValue(data))
                        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, DataType.values()[position % len]);
            }
        }
    }
}
