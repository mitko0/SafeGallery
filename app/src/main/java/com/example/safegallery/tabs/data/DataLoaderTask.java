package com.example.safegallery.tabs.data;

import android.content.ContentResolver;
import android.os.AsyncTask;
import com.example.safegallery.tabs.data.intefaces.DataSetter;

import java.io.File;
import java.util.*;

public class DataLoaderTask extends AsyncTask<DataType, Void, Void> {

    private final ContentResolver contentResolver;
    private final boolean safe;
    private final DataSetter dataSetter;

    public DataLoaderTask(ContentResolver contentResolver, boolean safe, DataSetter dataSetter) {
        this.safe = safe;
        this.dataSetter = dataSetter;
        this.contentResolver = contentResolver;
    }

    @Override
    protected Void doInBackground(DataType... dataTypes) {
        List<DataPath> paths;
        if (safe) {
            String path = StorageData.APP_SAFE_DATA_PATH + "Safe" + dataTypes[0].name();
            paths = StorageData.loadDataPathsForPath(path);
        } else
            paths = StorageData.loadDataPathsForMedia(this.contentResolver, dataTypes[0]);

        Map<String, List<DataPath>> data = new HashMap<>();
        for (DataPath path : paths) {
            File file = new File(path.getPath());
            String parent = file.getParent();

            List<DataPath> values = data.getOrDefault(parent, new ArrayList<>());
            values.add(path);
            data.put(parent, values);
        }

        this.dataSetter.setDataMaps(data);
        return null;
    }
}
