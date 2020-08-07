package com.example.safegallery.tabs.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
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
import com.example.safegallery.tabs.data.*;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import lombok.Builder;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

@Builder
public class FileFragment extends Fragment {

    List<DataPath> dataPaths;
    DataType dataType;
    boolean safe;

    Context context;
    BottomSheetBehavior<View> bsSelectTools;
    RecyclerView recyclerView;
    FileAdapter recyclerViewAdapter;

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
        this.recyclerViewAdapter.setDataType(this.dataType);
        this.recyclerViewAdapter.setSafe(this.safe);
        // this.recyclerViewAdapter.setDataPaths(this.dataPaths);

        if (safe)
            new DataLoaderTask<>(this.recyclerViewAdapter, this.context.getContentResolver(), true, "setDataPaths", "")
                    .execute(this.dataType);
        else
            new DataLoaderTask<>(this.recyclerViewAdapter, this.context.getContentResolver(), "setDataPaths", "")
                    .execute(this.dataType);

        this.recyclerViewAdapter.setClickListener(new ClickListener() {
            @Override
            public void onClick(File file, String mimeType) {
            }

            @Override
            public void onClick(DataPath dataPath) {
                if (safe)
                    openSafeFile(dataPath);
                else
                    openFile(dataPath);
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

        if (safe)
            view.findViewById(R.id.lock).setVisibility(View.GONE);
        else
            view.findViewById(R.id.unlock).setVisibility(View.GONE);

        this.recyclerView = view.findViewById(R.id.rvDataRecyclerView);
        this.recyclerView.setItemViewCacheSize(100);
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

    private void openFile(DataPath dataPath) {
        File file = new File(dataPath.getPath());
        Uri uri = FileProvider.getUriForFile(this.context, this.context.getApplicationContext().getPackageName() + ".provider", file);

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, dataPath.getMimeType());
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        this.startActivity(intent);
    }

    private void openSafeFile(DataPath dataPath) {
        SharedPreferences prefs = this.context.getSharedPreferences(this.context.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String extension = FilenameUtils.getExtension(dataPath.getPath());
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

        String prefsData = prefs.getString(mimeType, StorageData.TMP_FILE_NAME);

        String tmpName = StorageData.TMP_FOLDER + StorageData.TMP_FILE_NAME + extension;
        String fileName = StorageData.TMP_FILE_NAME;
        if (prefsData.equals(StorageData.TMP_FILE_NAME)) {
            tmpName = StorageData.TMP_FOLDER + StorageData.TMP_FILE_NAME_2 + extension;
            fileName = StorageData.TMP_FILE_NAME_2;
        }

        editor.putString(mimeType, fileName);
        editor.apply();

        File file = new File(tmpName);
        file.deleteOnExit();
        try {
            Files.write(file.toPath(), dataPath.getData());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Uri uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, dataPath.getMimeType());
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        this.startActivity(intent);
    }
}
