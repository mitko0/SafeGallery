package com.example.safegallery.tabs.fragments;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.safegallery.R;
import com.example.safegallery.recycler_views.interfaces.ClickListener;
import com.example.safegallery.tabs.data.DataPath;
import com.example.safegallery.tabs.data.DataType;
import com.example.safegallery.tabs.data.DataLoaderTask;

import java.io.File;

public class FolderFragment extends Fragment {

    private DataType dataType;
    private boolean safe = false;

    RecyclerView recyclerView;

    public FolderFragment() {}

    public FolderFragment(DataType dataType) {
        this.dataType = dataType;
    }


    // TODO: for safe data
    public FolderFragment(DataType dataType, boolean safe) {
        this.dataType = dataType;
        this.safe = safe;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*this.recyclerViewAdapter = new DefaultAdapter();
        this.recyclerViewAdapter.setContext(this.getContext());
        this.recyclerViewAdapter.setClickListener(new ClickListener() {
            @Override
            public void onClick(File file, String mimeType) {}

            @Override
            public void onClick(File file) {
                tabFragmentListener.onNextFragment();

                *//*fragmentManager.beginTransaction()
                        .replace(v.getId(), new FileFragment())
                        .addToBackStack("asd")
                        .commit();*//*

                   *//* List<DataPath> paths = new ArrayList<>();
                    for (String childPath : file.list()) {
                        DataPath dataPath = new DataPath(file.getAbsolutePath() + "/" + childPath, mimeType);
                        paths.add(dataPath);
                    }
                    recyclerViewAdapter.setDataPaths(paths);
                    recyclerViewAdapter.notifyDataSetChanged();*//*
                *//*else {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                Uri uri = FileProvider.getUriForFile(FolderFragment.this.getContext(), FolderFragment.this.getActivity().getApplicationContext().getPackageName() + ".provider", file);
                intent.setDataAndType(uri, mimeType);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                FolderFragment.this.startActivity(intent);
            }*//*
            }
        });*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        /*this.recyclerViewAdapter = new DefaultAdapter();
        this.recyclerViewAdapter.setContext(this.getContext());
        this.recyclerViewAdapter.setClickListener(new ClickListener() {
            @Override
            public void onClick(File file, String mimeType) {}

            @Override
            public void onClick(DataPath dataPath) {

            }
        });*/

        View view = inflater.inflate(R.layout.fragment_default, container, false);
        this.recyclerView = view.findViewById(R.id.rvDataRecyclerView);
        this.recyclerView.setHasFixedSize(true);
        this.recyclerView.setLayoutManager(new GridLayoutManager(this.getContext(), 3));
        /*this.recyclerView.setAdapter(this.recyclerViewAdapter);

        new DataLoaderTask<>(this.recyclerViewAdapter, this.getContext().getContentResolver(), "setDataPaths", "setViewDataPaths")
                .execute(this.dataType);*/
        return view;
    }
}
