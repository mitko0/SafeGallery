package com.example.safegallery.tabs.data;

import android.content.ContentResolver;
import android.os.AsyncTask;
import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.*;

public class DataLoaderTask<A extends RecyclerView.Adapter<VH>, VH extends RecyclerView.ViewHolder>
        extends AsyncTask<DataType, Void, List<String>> {

    private final A recyclerViewAdapter;
    private final ContentResolver contentResolver;
    private final String allDataSetter;
    private final String viewDataSetter;

    public DataLoaderTask(A recyclerViewAdapter, ContentResolver contentResolver, String allDataSetter, String viewDataSetter) {
        this.recyclerViewAdapter = recyclerViewAdapter;
        this.contentResolver = contentResolver;
        this.allDataSetter = allDataSetter;
        this.viewDataSetter = viewDataSetter;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected List<String> doInBackground(DataType... dataTypes) {
        List<DataPath> paths = StorageData.loadDataPathsForMedia(this.contentResolver, dataTypes[0]);

        Set<String> viewData = new HashSet<>();
        for (DataPath path : paths) {
            String filePath = path.getPath();
            File file = new File(filePath);
            viewData.add(file.getParent());
        }

        if (!this.allDataSetter.equals("")) {
            try {
                this.recyclerViewAdapter.getClass().getMethod(this.allDataSetter, List.class).invoke(this.recyclerViewAdapter, paths);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        return new ArrayList<>(viewData);
    }

    @Override
    protected void onPostExecute(List<String> viewData) {
        super.onPostExecute(viewData);
        if (!this.viewDataSetter.equals("")) {
            try {
                this.recyclerViewAdapter.getClass().getMethod(this.viewDataSetter, List.class).invoke(this.recyclerViewAdapter, viewData);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.recyclerViewAdapter.notifyDataSetChanged();
    }
}