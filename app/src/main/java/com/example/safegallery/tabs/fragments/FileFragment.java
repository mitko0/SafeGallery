package com.example.safegallery.tabs.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.safegallery.R;
import com.example.safegallery.recycler_views.interfaces.BottomSheetListener;
import com.example.safegallery.recycler_views.interfaces.ClickListener;
import com.example.safegallery.recycler_views.file.FileAdapter;
import com.example.safegallery.tabs.data.DataLoaderTask;
import com.example.safegallery.tabs.data.DataPath;
import com.example.safegallery.tabs.data.DataType;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.File;
import java.util.List;

public class FileFragment extends Fragment {

    private List<DataPath> dataPaths;

    // tmp
    private DataType dataType;

    Context context;
    BottomSheetBehavior<View> bsSelectTools;
    RecyclerView recyclerView;
    FileAdapter recyclerViewAdapter;

    public FileFragment() {
    }

    public FileFragment(DataType dataType) {
        this.dataType = dataType;
    }

    public FileFragment(@NonNull List<DataPath> dataPaths) {
        this.dataPaths = dataPaths;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.recyclerViewAdapter = new FileAdapter();
        this.recyclerViewAdapter.setContext(this.getContext());
        // this.recyclerViewAdapter.setDataPaths(this.dataPaths);

        new DataLoaderTask<>(this.recyclerViewAdapter, context.getContentResolver(), "setDataPaths", "")
                .execute(this.dataType);

        this.recyclerViewAdapter.setClickListener(new ClickListener() {
            @Override
            public void onClick(File file, String mimeType) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                Uri uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);
                intent.setDataAndType(uri, mimeType);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                FileFragment.this.startActivity(intent);
            }

            @Override
            public void onClick(File file) {
                // nut used here
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_default, container, false);
        LinearLayout bsSelectToolsView = view.findViewById(R.id.bottom_sheet);
        this.setChildrenClickListeners(bsSelectToolsView);

        this.recyclerView = view.findViewById(R.id.rvDataRecyclerView);
        this.recyclerView.setHasFixedSize(true);
        this.recyclerView.setLayoutManager(new GridLayoutManager(this.getContext(), 3));
        this.recyclerView.setAdapter(this.recyclerViewAdapter);

        this.bsSelectTools = BottomSheetBehavior.from(bsSelectToolsView);
        this.bsSelectTools.setState(BottomSheetBehavior.STATE_HIDDEN);
        this.recyclerViewAdapter.setBsSelectTools(this.bsSelectTools);

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        this.cancelSelecting();
    }

    private void setChildrenClickListeners(LinearLayout view) {
        for (int i = 0; i < view.getChildCount(); i++) {
            view.getChildAt(i).setOnClickListener(v -> {
                BottomSheetListener listener = this.recyclerViewAdapter;
                listener.onChildClick(v.getId(), getParentFragmentManager());
            });
        }
    }

    private void cancelSelecting() {
        this.recyclerViewAdapter.setSelecting(false);
        this.recyclerViewAdapter.notifyDataSetChanged();
        this.bsSelectTools.setState(BottomSheetBehavior.STATE_HIDDEN);
    }
}
