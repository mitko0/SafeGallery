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
import com.example.safegallery.tabs.data.DataType;
import com.example.safegallery.tabs.viewmodels.DataViewModel;
import com.example.safegallery.tabs.viewmodels.DataViewModelFactory;

public class ParentFragment extends Fragment {

    private int position;
    private boolean safe;
    private DataType dataType;

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

        DataViewModel viewModel = new ViewModelProvider(this.requireActivity(), new DataViewModelFactory(this.requireActivity().getApplication())).get(DataViewModel.class);
        viewModel.loadData(this.position);

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
}
