package com.example.safegallery.tabs.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.safegallery.R;
import com.example.safegallery.recycler_views.defaultt.DefaultAdapter;
import com.example.safegallery.tabs.data.DataPath;
import com.example.safegallery.tabs.data.DataType;
import com.example.safegallery.tabs.data.StorageData;

import java.util.List;

public class BlankFragment extends Fragment {

    private DataType dataType;
    private boolean safe = false;

    RecyclerView recyclerView;
    DefaultAdapter recyclerViewAdapter;

    public BlankFragment(DataType dataType) {
        this.dataType = dataType;
    }

    public BlankFragment(DataType dataType, boolean safe) {
        this.dataType = dataType;
        this.safe = safe;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.recyclerViewAdapter = new DefaultAdapter();
        this.recyclerViewAdapter.setContext(this.getContext());
        this.recyclerViewAdapter.setClickListener((file, mimeType) -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            Uri uri = FileProvider.getUriForFile(this.getContext(), this.getActivity().getApplicationContext().getPackageName() + ".provider", file);
            intent.setDataAndType(uri, mimeType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_blank, container, false);
        this.recyclerView = view.findViewById(R.id.recyclerFolders);
        this.recyclerView.setHasFixedSize(true);
        this.recyclerView.setLayoutManager(new GridLayoutManager(this.getContext(), 3));
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        this.asd();
    }

    private void asd() {
        List<DataPath> paths = StorageData.loadDataPathsForMedia(this.getContext(), this.dataType);
        recyclerViewAdapter.setPaths(paths);
        recyclerView.setAdapter(recyclerViewAdapter);
    }

}
