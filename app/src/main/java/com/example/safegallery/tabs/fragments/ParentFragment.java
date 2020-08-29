package com.example.safegallery.tabs.fragments;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;
import com.example.safegallery.R;
import com.example.safegallery.recycler_views.interfaces.ViewModelListener;
import com.example.safegallery.tabs.data.DataEncryptorTask.DataHolder;
import com.example.safegallery.tabs.data.DataPath;
import com.example.safegallery.tabs.data.DataType;
import com.example.safegallery.tabs.viewmodels.DataViewModel;
import com.example.safegallery.tabs.viewmodels.DataViewModelFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ParentFragment extends Fragment
        implements ViewModelListener {

    private int position;
    private boolean safe;
    private DataType dataType;
    DataViewModel viewModel;

    Context context;

    public ParentFragment() {
        // required
    }

    public ParentFragment(int position, boolean safe, DataType dataType) {
        this.position = position;
        this.safe = safe;
        this.dataType = dataType;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.viewModel = new ViewModelProvider(this.requireActivity(), new DataViewModelFactory(this.requireActivity().getApplication())).get(DataViewModel.class);
        // this.viewModel.loadData(this.position);
        this.viewModel.loadAllData();

        Fragment fragment = new FolderFragment(this.position, this.safe, this.dataType);
        this.getChildFragmentManager()
                .beginTransaction()
                .add(R.id.childHolder, fragment, fragment.toString())
                .commit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_parent, container, false);
    }

    public int getPosition() {
        return this.position;
    }

    @Override
    public void updateData(List<DataHolder> data) {
        int len = DataType.values().length;
        int updatePosition = this.position < len
                ? this.position + len
                : this.position - len;

        for (DataHolder item : data) {
            File file = new File(item.getValue());

            Map<String, List<DataPath>> dataMap = this.viewModel.getDataMap(updatePosition).getValue();
            if (dataMap != null) {
                List<DataPath> value = dataMap.getOrDefault(file.getParent(), new ArrayList<>());
                if (!value.stream()
                        .map(DataPath::getPath)
                        .collect(Collectors.toList())
                        .contains(item.getValue())) {

                    value.add(new DataPath(item.getValue(), item.getDataPath().getMimeType(), item.getData()));
                }
                dataMap.put(file.getParent(), value);
            }
        }
    }
}
